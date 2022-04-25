package edu.duke.online.tenki;

import edu.duke.online.tenki.generated.DukeWeatherContract;
import edu.duke.online.tenki.model.FromToAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("api")
public class TenkiController {

    @Autowired
    Web3j web3j;

    @Autowired
    Services services;


    @GetMapping(value="info")
    public Map<String, Object> getInfo(){
        Map<String, Object> info = new LinkedHashMap<>();
        try {
            // web3_clientVersion returns the current client version.
            Web3ClientVersion clientVersion = web3j.web3ClientVersion().send();
            // eth_blockNumber returns the number of most recent block.
            EthBlockNumber blockNumber = web3j.ethBlockNumber().send();
            // eth_gasPrice, returns the current price per gas in wei.
            EthGasPrice gasPrice = web3j.ethGasPrice().send();

            info.put("Client version" , clientVersion.getWeb3ClientVersion());
            info.put("Block number" , blockNumber.getBlockNumber());
            info.put("Gas price" , gasPrice.getGasPrice());

        } catch (IOException ex) {
            throw new RuntimeException("Error sending json-rpc requests", ex);
        }
        return info;
    }


    /*
    {
  "fromAccountWalletPassword": "costco",
  "fromAccount": "0xdbCB14153884C8871165e715F7688E06a47933CE",
  "toAccount": "0xBDC9e87BbC25149139e2bA4ba9a4f3B3FdCfafF3"
}
     */

    @PostMapping("transferEther/{amount}")
    Map<String, Object> transferEther(@PathVariable("amount") BigDecimal amount,
                                      @RequestBody FromToAccount accounts) {
        return services.transferEther(amount, accounts);
    }


    @GetMapping("insurance/{contractAddress}/{userAddress}")
    Map<String, Object> getInsuranceInfo(
            @PathVariable("contractAddress") String contractAddress,
            @PathVariable("userAddress") String userAddress,
            @RequestParam("walletPassword") String walletPassword){
        Map<String, Object> data = null;
        try{
          DukeWeatherContract dukeWeatherContract =  services.getDukeWeatherContract(contractAddress, userAddress, walletPassword);
            TransactionReceipt transactionReceipt = dukeWeatherContract.emitInformation().send();
            data = services.extractInfoFromSwap(dukeWeatherContract, transactionReceipt);
        }
        catch (Exception e){
            throw new RuntimeException("Unable to get information on contract " + e.getMessage());
        }

        return data;
    }


    @PostMapping("purchaseRisk/{contractAddress}/{buyerAddress}")
    Map<String, Object> purchaseRisk(@PathVariable("contractAddress") String contractAddress,
                                           @PathVariable("buyerAddress") String buyerAddress,
                                           @RequestParam("amountOfRisk") BigDecimal riskAmount,
                                           @RequestParam("walletPassword") String walletPassword) {
        Map<String, Object> data = null;
        try {
            DukeWeatherContract dukeWeatherContract = services.getDukeWeatherContract(contractAddress, buyerAddress, walletPassword);
            String tenkiAddress = "0xEc10233C905e647fEd29144A4e411c1d7E311264";
//            TransactionReceipt transactionReceipt = dukeWeatherContract.buyTheRisk(
//                    tenkiAddress, Convert.toWei(riskAmount.toString(), Convert.Unit.ETHER).toBigInteger()).send();
//            data = services.extractInfoFromSwap(dukeWeatherContract, transactionReceipt);
        }
        catch(Exception e){
            throw new RuntimeException("Unable to buy into risk" + e);
        }
        return data;
    }

            @PostMapping("catastrophicEventHappened/{contractAddress}/{tenkiAddress}")
    Map<String, Object> endWithCatastrophe(@PathVariable("contractAddress") String contractAddress,
                                            @PathVariable("tenkiAddress") String tenkiAddress,
                                            @RequestParam("walletPassword") String walletPassword){
        Map<String, Object> data = null;
        try{
            DukeWeatherContract dukeWeatherContract =  services.getDukeWeatherContract(contractAddress, tenkiAddress, walletPassword);
            TransactionReceipt transactionReceipt = dukeWeatherContract.catastrophicEventHappened().send();
            data = services.extractInfoFromSwap(dukeWeatherContract, transactionReceipt);

            FromToAccount fromToAccount = new FromToAccount();
            fromToAccount.setFromAccount(tenkiAddress);
            fromToAccount.setToAccount(data.get("InsuredAddress").toString());
            fromToAccount.setFromAccountWalletPassword("costco");
            BigDecimal balance = new BigDecimal( data.get("Indemnity").toString() ) ;
            BigDecimal premium = new BigDecimal( data.get("Premium").toString());
            BigDecimal amountToMove = balance.subtract( premium );
            amountToMove.multiply(BigDecimal.valueOf(95)).divide(BigDecimal.valueOf(100));
            //Transfer some tokens so that it can pay for gas
            System.err.println(  transferEther(amountToMove, fromToAccount));

        }
        catch (Exception e){
            throw new RuntimeException("Unable to get information on contract " + e.getMessage());
        }


        return data;
    }


    @PostMapping("createNewInsurance/{ownerAddress}")
    Map<String, Object> insurance(@PathVariable("ownerAddress") String ownerAddress,
                                  @RequestParam("contractStartDate") Date contractStartDate,
                                  @RequestParam("contractEndDate") Date contractEndDate,
                                  @RequestParam("indemnity") BigDecimal indemnity,
                                  @RequestParam("minimumProtection") BigDecimal minimumProtection,
                                  @RequestParam("region") String  region,
                                  @RequestParam("upperTemperatureBoundary") BigDecimal upperDeviationFromAvg,
                                  @RequestParam("lowerTemperatureBoundary")BigDecimal lowerDeviationFromAvg,
                                  @RequestParam("averageTemperature") BigDecimal averageTemperature,
                                  @RequestParam("walletPassword") String walletPassword){

            return services.createNewInsuranceContract(
                    ownerAddress,
                    contractStartDate,
                    contractEndDate,
                    Convert.toWei(indemnity, Convert.Unit.ETHER),
                    Convert.toWei(minimumProtection, Convert.Unit.ETHER),
                    region,
                    upperDeviationFromAvg,
                    lowerDeviationFromAvg,
                    averageTemperature,
                    walletPassword
            );
    }


    }
