package edu.duke.online.tenki;

import edu.duke.online.tenki.generated.DukeWeatherContract;
import edu.duke.online.tenki.generated.Insurance;
import edu.duke.online.tenki.model.DukeWallet;
import edu.duke.online.tenki.model.FromToAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class Services {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Value("${duke.chainId}")
    Integer chainId;

    @Value("${duke.datadir}")
    String dataDir;

    @Value("${duke.tenkiAddress}")
    String tenkiAddress;

    //This is for convinience -- bad idea in prod!
    @Value("${duke.defaultWalletPassword}")
    String defaultWalletPassword;


    @Autowired
    Web3j web3j;


    DukeWallet createWallet(@RequestParam("nickName") String nickName,
                            @RequestParam("password") String password) {
        DukeWallet dukeWallet = new DukeWallet();
        try {
            String directory = dataDir + "/keystore";
            String fileName = WalletUtils.generateNewWalletFile(password, new File(directory));
            String walletFilePath = directory + "/" + fileName;
            Credentials credentials = WalletUtils.loadCredentials(password, new File(walletFilePath));
            dukeWallet.setPublicAddress(credentials.getAddress());
            dukeWallet.setWalletName(nickName);
            dukeWallet.setPassword(password);
            dukeWallet.setPublicKey(
                    Numeric.toHexStringWithPrefix(credentials.getEcKeyPair().getPublicKey()));
            dukeWallet.setPrivateKey(
                    Numeric.toHexStringWithPrefix(credentials.getEcKeyPair().getPrivateKey()));
            SimpleJdbcInsert simpleJdbcInsert =
                    new SimpleJdbcInsert(jdbcTemplate).withTableName("WALLET");
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("ID", dukeWallet.getPublicAddress().toUpperCase());
            parameters.put("NAME", nickName);
            parameters.put("FILEPATH", walletFilePath);
            parameters.put("PASSWORD", password);
            simpleJdbcInsert.execute(parameters);
        } catch (Exception e) {
            throw new RuntimeException("Unable to create wallet : " + e.getMessage());
        }

        return dukeWallet;
    }

    Map<String, Object> createNewInsuranceContract(String ownerAddress,
                                                   Date contractStartDate,
                                                   Date contractEndDate,
                                                   BigDecimal premium,
                                                   BigDecimal indemnity,
                                                   BigDecimal minimumProtection,
                                                   String region,
                                                   BigDecimal upperDeviationFromAvg,
                                                   BigDecimal lowerDeviationFromAvg,
                                                   BigDecimal averageTemperature,
                                                   String walletPassword) {
        Map<String, Object> data = new LinkedHashMap<>();
        try {
            TransactionManager transactionManager = getTransactionManager(getCredentials(tenkiAddress, "costco"));

            String date = new Date(System.currentTimeMillis()).toString();
            String s = (region + date + averageTemperature) .replaceAll(" ", "").toUpperCase();
            s =         s.substring(0, Math.min(s.length(), 10));
            Insurance premiumToken = Insurance.deploy(web3j,
                    transactionManager, new DefaultGasProvider(),
                    region + averageTemperature, "P"+  s ,
                    Convert.fromWei(premium, Convert.Unit.ETHER).toBigInteger()).send();
            Insurance indemnityToken = Insurance.deploy(web3j,
                    transactionManager, new DefaultGasProvider(),
                    region + averageTemperature, "I" + s ,
                    Convert.fromWei(indemnity, Convert.Unit.ETHER).toBigInteger()).send();



            DukeWeatherContract dwc = DukeWeatherContract.deploy(web3j, transactionManager
                    , new DefaultGasProvider(),
                    premiumToken.getContractAddress(),
                    indemnityToken.getContractAddress()).send();


//            FromToAccount fromToAccount = new FromToAccount();
//            fromToAccount.setFromAccount(tenkiAddress);
//            fromToAccount.setToAccount(premiumToken.getContractAddress());
//            fromToAccount.setFromAccountWalletPassword("costco");
//            //Transfer some tokens so that it can pay for gas
//            System.err.println(  transferEther( BigDecimal.valueOf( 1000 * 1000), fromToAccount) );

            //Now create the new contract under the ownerAddress
            DukeWeatherContract thisSwap =
                    getDukeWeatherContract(dwc.getContractAddress(), ownerAddress, walletPassword);

            thisSwap.createNewInsuranceContract(
                    BigDecimal.valueOf(contractStartDate.getTime()).toBigInteger(),
                    BigDecimal.valueOf(contractEndDate.getTime()).toBigInteger(),
                    indemnity.toBigInteger(),
                    minimumProtection.toBigInteger(),
                    region,
                    upperDeviationFromAvg.toBigInteger(),
                    lowerDeviationFromAvg.toBigInteger(),
                    averageTemperature.toBigInteger()).send();

            //Now pledge the premium amount
            TransactionReceipt transactionReceipt = thisSwap.pledgePremium(premium.toBigInteger()).send();
            data = extractInfoFromSwap(thisSwap, transactionReceipt);
//            data.put("PremiumTokenAddress", premiumToken.getContractAddress());
//            data.put("IndemnityTokenAddress", indemnityToken.getContractAddress());

            //Now persist it to our local database
            new SimpleJdbcInsert(jdbcTemplate)
                    .withTableName("EXCHANGE_LISTING").
                    execute(data);

        } catch (Exception e) {
            throw new RuntimeException("Unable to create new contract " + e.getMessage());
        }
        return data;
    }

    public Map<String, Object> extractInfoFromSwap(DukeWeatherContract dukeWeatherContract,
                                                   TransactionReceipt transactionReceipt) {

        Map<String, Object> data = new LinkedHashMap<>();
        List<DukeWeatherContract.SwapCreationEventResponse> swaps =
                dukeWeatherContract.getSwapCreationEvents(transactionReceipt);
        for (DukeWeatherContract.SwapCreationEventResponse swap : swaps) {
            data.put("ContractId", dukeWeatherContract.getContractAddress());
            data.put("StartDate", new Date(swap.contractStartDate.longValue()));
            data.put("EndDate", new Date(swap.contractEndDate.longValue()));
            data.put("Region", swap.region);
            data.put("Premium", Convert.fromWei( String.valueOf(swap.premium)   , Convert.Unit.ETHER).longValue());
            data.put("Indemnity", Convert.fromWei( String.valueOf(swap.indemnity)   , Convert.Unit.ETHER).longValue());
            data.put("MinimumIndemnity", Convert.fromWei( String.valueOf(swap.minimumIndemnity)   , Convert.Unit.ETHER).longValue());
            data.put("UpperTemperature", swap.upperDeviationFromAvg);
            data.put("LowerTemperature", swap.lowerDeviationFromAvg);
            data.put("AverageTemperature", swap.averageTemperature);
            data.put("RemainingIndemnity", swap.remainingIndemnity);
            data.put("InsuredAddress", swap.insuredAddress);
            data.put("CurrentBalance", swap.currentBalance);
            data.put("Status", swap.status);
            data.put("InsuranceProvider", swap._indemnityProviders);
            try {
                data.put("PremiumTokenAddress", dukeWeatherContract.getPremiumToken().send());
                data.put("IndemnityTokenAddress", dukeWeatherContract.getIndemnityToken().send());
            }
            catch(Exception e){
                //Ignore these ....
            }
        }
        return data;
    }

    public DukeWeatherContract getDukeWeatherContract(String contractAddress, String ownerAddress, String walletPassword) {
        Credentials credentials = getCredentials(ownerAddress, walletPassword);
        TransactionManager transactionManager = new FastRawTransactionManager(
                web3j, credentials, 1337);
        DukeWeatherContract dukeWeatherContract = DukeWeatherContract.load(contractAddress,
                web3j, transactionManager, new DefaultGasProvider());
        return dukeWeatherContract;
    }

    Map<String, Object> getLocalAccountInfo(String publicAddress,
                                            String password) {
        DukeWallet dukeWallet = new DukeWallet();
        //TODO Fix the following as it allows for sql injection  also
        // keeping clear case password is BAD but ok, for playing around
        return jdbcTemplate.queryForMap("select * from walletdb.wallet where ID = '" + publicAddress.toUpperCase() + "'");
        //+ " and password = '" + password + "'");
    }

    Credentials getCredentials(String publicAddress, String password) {
        //get the wallet info
        Map<String, Object> data = getLocalAccountInfo(publicAddress, password);
        try {
            String fileName = data.get("FILEPATH").toString();
            return WalletUtils.loadCredentials(password,
                    new File(fileName));
        } catch (Exception e) {
            throw new RuntimeException("Unable to get credentials for " + publicAddress);
        }
    }

    TransactionManager getTransactionManager(Credentials credentials) {
        return new RawTransactionManager(
                web3j, credentials, chainId);
    }


    Map<String, Object> transferEther(BigDecimal amount,
                                      FromToAccount accounts) {
        Map<String, Object> data = new LinkedHashMap<>();
        Credentials credentials = getCredentials(accounts.getFromAccount(), accounts.getFromAccountWalletPassword());
        try {
            TransactionReceipt transactionReceipt = Transfer.sendFundsEIP1559(
                    web3j, credentials,
                    accounts.getToAccount(), //toAddress
                    amount, //value
                    Convert.Unit.WEI, //unit
                    BigInteger.valueOf(8_000_000),
                    DefaultGasProvider.GAS_LIMIT, //maxPriorityFeePerGas (max fee per gas transaction willing to give to miners)
                    BigInteger.valueOf(3_100_000_000L) //maxFeePerGas (max fee transaction willing to pay)
            ).send();
            data.put("NumberOf Ethers", amount);
            data.put("FromAccount", accounts.getFromAccount());
            data.put("ToAccount", accounts.getToAccount());
            data.put("Transaction#", transactionReceipt.getTransactionHash());
            data.put("Gas Used", transactionReceipt.getGasUsed());
        } catch (Exception e) {
            throw new RuntimeException("Unable to transfer ether" + e.getMessage());
        }
        return data;
    }

    public static Bytes32 stringToBytes32(String string) {
        byte[] byteValue = string.getBytes();
        byte[] byteValueLen32 = new byte[32];
        System.arraycopy(byteValue, 0, byteValueLen32, 0, byteValue.length);
        return new Bytes32(byteValueLen32);
    }


    public Map<String, Object> populateH2(String directory) {
        Map<String, Object> data = new LinkedHashMap<>();
        try {
            String[] walletNames = {"Tenki Custody Account", "Farmer", "Insurer 1", "Insurer 2", "Insurer 3"};
            String sql = "insert into walletdb.wallet (ID, NAME, PASSWORD, FILEPATH) values (?,?,?,?)";
            File dir = new File(directory);
            int i = -1;
            jdbcTemplate.update("delete walletdb.wallet");
            for (File f : dir.listFiles()) {
                String name = ++i >= walletNames.length ? "Insurer " + i : walletNames[i];
                String ID = f.getCanonicalPath().substring(f.getCanonicalPath().lastIndexOf("--") + 2);
                ID = "0X" + ID.replaceAll(".json", "").stripTrailing().toUpperCase();
                data.put(ID + " : " + name, f.getCanonicalPath());
                jdbcTemplate.update(sql, ID, name, "costco", f.getCanonicalPath());

            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to transfer ether" + e.getMessage());
        }
        return data;
    }


}
