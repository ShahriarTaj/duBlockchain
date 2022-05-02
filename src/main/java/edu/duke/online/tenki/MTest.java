package edu.duke.online.tenki;

import edu.duke.online.tenki.generated.DukeWeatherContract;
import edu.duke.online.tenki.generated.Insurance;
import edu.duke.online.tenki.generated.Syndicated;
import edu.duke.online.tenki.generated.TST;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MTest {

    public static void main(String[] args) throws Exception{
        Web3j w = Web3j.build(new HttpService());
        Credentials insuranceCompany = WalletUtils.loadCredentials("costco",
                new File("/Users/shahriartaj/DukeCourse/test-chain-dir/keystore//UTC--2022-04-16T17-47-03.15001000Z--ec10233c905e647fed29144a4e411c1d7e311264.json"));

        Credentials protectionBuyer = WalletUtils.loadCredentials("costco",
                new File("/Users/shahriartaj/DukeCourse/test-chain-dir/keystore//UTC--2022-04-12T01-44-14.931466000Z--6991381b245330a93b63bc6be5c2a8d3d21f2a83"));

        Credentials riskBuyer = WalletUtils.loadCredentials("costco",
                new File("/Users/shahriartaj/DukeCourse/test-chain-dir/keystore/UTC--2022-04-16T18-45-41.629605000Z--dbcb14153884c8871165e715f7688e06a47933ce.json"));

        TransactionManager tmc = new FastRawTransactionManager(
                w, insuranceCompany, 1337);

        TransactionManager tmpb = new FastRawTransactionManager(
                w, protectionBuyer, 1337);
        TransactionManager tmrb = new FastRawTransactionManager(
                w, riskBuyer, 1337);

        Syndicated syndicated = Syndicated.deploy(w, tmc, new DefaultGasProvider()).send();
        System.err.println("SYN: " + syndicated.getContractAddress());
        System.err.println("Premium: " + syndicated.getPremiumToken().send());
        System.err.println("Indemnity : " + syndicated.getIndemnityToken().send());

        Insurance premiumBuy = Insurance.load(syndicated.getPremiumToken().send(), w, tmpb, new DefaultGasProvider());
        System.err.println(premiumBuy.totalSupply().send());


    }

    public static void ignore2main(String[] args) throws Exception{
        Web3j w = Web3j.build(new HttpService());
        Credentials insuranceCompany = WalletUtils.loadCredentials("costco",
                new File("/Users/shahriartaj/DukeCourse/test-chain-dir/keystore//UTC--2022-04-16T17-47-03.15001000Z--ec10233c905e647fed29144a4e411c1d7e311264.json"));

        Credentials protectionBuyer = WalletUtils.loadCredentials("costco",
                new File("/Users/shahriartaj/DukeCourse/test-chain-dir/keystore//UTC--2022-04-12T01-44-14.931466000Z--6991381b245330a93b63bc6be5c2a8d3d21f2a83"));

        Credentials riskBuyer = WalletUtils.loadCredentials("costco",
                new File("/Users/shahriartaj/DukeCourse/test-chain-dir/keystore/UTC--2022-04-16T18-45-41.629605000Z--dbcb14153884c8871165e715f7688e06a47933ce.json"));

        TransactionManager tmc = new FastRawTransactionManager(
                w, insuranceCompany, 1337);

        TransactionManager tmpb = new FastRawTransactionManager(
                w, protectionBuyer, 1337);
        TransactionManager tmrb = new FastRawTransactionManager(
                w, riskBuyer, 1337);
        Insurance test = Insurance.deploy(w,tmc, new DefaultGasProvider(), "XX1", "X", BigInteger.valueOf(110)).send();

        String contractAddress = test.getContractAddress();
        System.err.println(contractAddress);

        System.err.println("Name: " + test.name().send());
        System.err.println("Symbol: " + test.symbol().send());
        System.err.println("Owner's balance: " + test.balanceOf(insuranceCompany.getAddress()).send());
        System.err.println("TotalSupply: " + test.totalSupply().send());

        //TransactionReceipt x = test.transfer("0x6991381b245330a93b63bc6be5c2a8d3d21f2a83", BigInteger.valueOf(15)).send();
//        Insurance protectionBuyers = Insurance.load(contractAddress, w, tmpb, new DefaultGasProvider());
//        TransactionReceipt x = protectionBuyers.buy(Convert.toWei(BigDecimal.valueOf(10), Convert.Unit.ETHER).toBigInteger()).send();
//        List<Insurance.BoughtEventResponse> e = test.getBoughtEvents(x);
//        System.err.println("from : " + e.get(0).amountToBuy);
//        System.err.println("Owner's balance: " + test.balanceOf(insuranceCompany.getAddress()).send());
//        System.err.println("Insured's balance: " + test.balanceOf(protectionBuyer.getAddress()).send());
//        System.err.println("Contract Address: " + test.getContractAddress());

    }

    public static void oldmain(String[] args) throws Exception{
        Web3j w = Web3j.build(new HttpService());
        Credentials insuranceCompany = WalletUtils.loadCredentials("costco",
                new File("/Users/shahriartaj/DukeCourse/test-chain-dir/keystore//UTC--2022-04-16T17-47-03.15001000Z--ec10233c905e647fed29144a4e411c1d7e311264.json"));

        Credentials protectionBuyer = WalletUtils.loadCredentials("costco",
                new File("/Users/shahriartaj/DukeCourse/test-chain-dir/keystore//UTC--2022-04-12T01-44-14.931466000Z--6991381b245330a93b63bc6be5c2a8d3d21f2a83"));

        Credentials riskBuyer = WalletUtils.loadCredentials("costco",
                new File("/Users/shahriartaj/DukeCourse/test-chain-dir/keystore/UTC--2022-04-16T18-45-41.629605000Z--dbcb14153884c8871165e715f7688e06a47933ce.json"));

        TransactionManager tmc = new FastRawTransactionManager(
                w, insuranceCompany, 1337);

        TransactionManager tmpb = new FastRawTransactionManager(
                w, protectionBuyer, 1337);
        TransactionManager tmrb = new FastRawTransactionManager(
                w, riskBuyer, 1337);

        TST test = TST.deploy(w,tmc, new DefaultGasProvider(), "XX1", BigInteger.valueOf(100)).send();
        String contractAddress = test.getContractAddress();
        System.err.println(test.getContractAddress());
        Map<String, Object> data = new LinkedHashMap<>();
        //String contractAddress= "0x67a206e4af3385aef1b412ddc78147331e3d7d31";
        BigDecimal amount = new BigDecimal("1");
        TransactionReceipt transactionReceipt = Transfer.sendFundsEIP1559(
                w, insuranceCompany,
                contractAddress, //toAddress
                amount, //value
                Convert.Unit.ETHER, //unit
                BigInteger.valueOf(8_000_000),
                DefaultGasProvider.GAS_LIMIT, //maxPriorityFeePerGas (max fee per gas transaction willing to give to miners)
                BigInteger.valueOf(3_100_000_000L) //maxFeePerGas (max fee transaction willing to pay)
        ).send();
        data.put("NumberOf Ethers" ,  amount);
        data.put("FromAccount", "ec10233c905e647fed29144a4e411c1d7e311264");
        data.put("ToAccount", contractAddress);
        data.put("Transaction#", transactionReceipt.getTransactionHash());
        data.put("Gas Used",transactionReceipt.getGasUsed());
        System.err.println(data);

        TST protectionBuyers = TST.load(contractAddress, w, tmpb, new DefaultGasProvider());

//        TransactionReceipt x = protectionBuyers.buyProtection(BigInteger.valueOf(5)).send();
//        List<TST.TransferEventResponse> e = protectionBuyers.getTransferEvents(x);
//        System.err.println("from : " + e.get(0).from + "  ->  to : " + e.get(0).to + "   :   " +  e.get(0).tokens);

//        TST riskBuyers = TST.load(contractAddress, w, tmrb, new DefaultGasProvider());
//
//        x = riskBuyers.buyProtection(Convert.toWei("20", Convert.Unit.ETHER).toBigInteger()).send();
//        e = riskBuyers.getTransferEvents(x);
//        System.err.println(e.get(0).to + "  -> " + e.get(0).from + "   :   " +  e.get(0).tokens);
    }

    public static void imain(String[] args) throws Exception{
//        Web3j w = Web3j.build(new HttpService());
//        Credentials insuranceOwner = WalletUtils.loadCredentials("costco",
//                new File("/Users/shahriartaj/DukeCourse/test-chain-dir/keystore/UTC--2022-04-16T18-45-41.629605000Z--dbcb14153884c8871165e715f7688e06a47933ce.json"));
//
//        Credentials insuranceBuyer = WalletUtils.loadCredentials("costco",
//                new File("/Users/shahriartaj/DukeCourse/test-chain-dir/keystore//UTC--2022-04-12T01-44-14.931466000Z--6991381b245330a93b63bc6be5c2a8d3d21f2a83"));
//
//        TransactionManager tm = new FastRawTransactionManager(
//                w, insuranceOwner, 1337);
//
//
//
//         DukeWeatherContract d = DukeWeatherContract.deploy(w,tm, new DefaultGasProvider()).send();
//        //DukeWeatherContract d = DukeWeatherContract.load("0xBc385732dd709475Da60fb7d57A4623537B2e7A6", w, tm, new DefaultGasProvider());
//        List<DukeWeatherContract.SwapCreationEventResponse> e = null;
//        TransactionReceipt x = null;
//        TransactionReceipt y = d.createNewInsuranceContract(
//                BigInteger.valueOf(System.currentTimeMillis()),
//                BigInteger.valueOf(System.currentTimeMillis() + (3600 * 1000 * 2)),
//                BigInteger.valueOf(10L),
//                BigInteger.valueOf(1L),
//                "Northeast",
//                BigInteger.valueOf(70),
//                BigInteger.valueOf(5),
//                BigInteger.valueOf(-5)).send();
//
//        e = d.getSwapCreationEvents(y);
//        for ( DukeWeatherContract.SwapCreationEventResponse r : e){
//            System.err.println("SwapInfo: " + r.remainingIndemnity + " : " + r.region + " : " + r.minimumIndemnity);
//        }
//


//        System.err.println("Buying into contract");
//        TransactionManager tmb = new FastRawTransactionManager(
//                w, insuranceOwner, 1337);
//        DukeWeatherContract db = DukeWeatherContract.load(d.getContractAddress(),
//                w, tmb,  new DefaultGasProvider());
//        x = db.buyIntoContract(BigInteger.valueOf(1)).send();
//        e = db.getSwapCreationEvents(x);
//        for ( DukeWeatherContract.SwapCreationEventResponse r : e){
//            System.err.println("SwapInfo: " + r.remainingIndemnity + " : " + r.region);
//        }
//
//        d.buyIntoContract(BigInteger.valueOf(2000)).send();
//        System.err.println("InsurersCount: " + d.insureresCount().send());
//
//        e = d.getSwapCreationEvents(x);
//        for ( DukeWeatherContract.SwapCreationEventResponse r : e){
//            System.err.println("SwapInfo: " + r.remainingIndemnity + " : " + r.region);
//        }

//        System.err.println("Contract Address: " + d.getContractAddress());

    }

    public static void ignoremain(String[] args) throws Exception {
        Web3j w = Web3j.build(new HttpService());
        Credentials c = WalletUtils.loadCredentials("costco",
                new File("/Users/shahriartaj/DukeCourse/test-chain-dir/keystore/UTC--2022-04-16T18-45-41.629605000Z--dbcb14153884c8871165e715f7688e06a47933ce.json"));
        System.err.println(c.getAddress());
        System.err.println(c.getEcKeyPair().getPrivateKey().toString(16));
        System.err.println( Numeric.toHexStringWithPrefix(c.getEcKeyPair().getPrivateKey()));

        TransactionReceipt transactionReceipt = Transfer.sendFundsEIP1559(
                w, c,
                "0xbaD83aCB430508704923e2B9FA4Ad5Be57609a46", //toAddress
                BigDecimal.ONE.valueOf(1), //value
                Convert.Unit.ETHER, //unit
                BigInteger.valueOf(8_000_000),
                DefaultGasProvider.GAS_LIMIT, //maxPriorityFeePerGas (max fee per gas transaction willing to give to miners)
                BigInteger.valueOf(3_100_000_000L) //maxFeePerGas (max fee transaction willing to pay)
        ).send();
    }
}

