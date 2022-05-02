// SPDX-License-Identifier: GPL-3.0
pragma solidity ^0.8.00;

import "./TempSwap.sol";


contract Syndicated {
    struct WeatherSwap {
        uint contractStartDate;
        uint contractEndDate;
        uint256 premium;
        uint256 indemnity;
        uint256 remainingIndemnity;
        uint256 minimumIndemnity;
        string region;
        int upperDeviationFromAvg;
        int lowerDeviationFromAvg;
        int averageTemperature;
        int status;
        uint256 currentBalance;
        TempSwap premiumToken;
        TempSwap indemnityToken;
    }

    WeatherSwap  thisSwap;
    address tenkiCompanyAccount = 0xEc10233C905e647fEd29144A4e411c1d7E311264;
    /**
 * @dev This modifier checks that only the creator of the contract can call this smart contract
   */
    modifier onlyIfCreator {
        if (msg.sender == tenkiCompanyAccount) _;
    }

    event SwapCreation(
        uint contractStartDate,
        uint contractEndDate,
        uint256 premium,
        uint256 indemnity,
        uint256 remainingIndemnity,
        uint256 minimumIndemnity,
        string region,
        int upperDeviationFromAvg,
        int lowerDeviationFromAvg,
        int averageTemperature,
    // status with codes below
        int status,
    // 0 premium not even pledged
    // 1 pledged  but not funded (no premium has been sent)
    // 2 indemnity pledged but pledges don't reach minimum
    // 3 indemnity minimum has been reached but not funded
    // 4 indemnity minimum is funded
    // 5 ended with "event" happened and the protection buyer must be paid
    // 6 ended without the "event" happening, the risk buyer (insurers) must be paid
        uint256 currentBalance,
        TempSwap premiumToken,
        TempSwap indemnityToken
    );

    constructor()  {
        require(msg.sender == tenkiCompanyAccount);
        _premiumToken = new TempSwap ( "Premium", "Premium" , 100, tenkiCompanyAccount);
        _indemnityToken = new TempSwap ("Indemnity", "Indemnity", 100, tenkiCompanyAccount);
        _premiumTokenPrice = 0;
        _indemnityTokenPrice = 0;
    }

    TempSwap _premiumToken ;
    function getPremiumToken() external view  returns (TempSwap premiumTokenAddress){
        return _premiumToken;
    }
    TempSwap _indemnityToken;
    function getIndemnityToken() external view  returns (TempSwap indemnityTokenAddress){
        return _indemnityToken;
    }
    uint256 _premiumTokenPrice;
    uint256 _indemnityTokenPrice;

    function createNewTempSwapContract(
        uint contractStartDate,
        uint contractEndDate,
        uint256 indemnity,
        uint256 minimumIndemnity,
        string  memory region,
        int upperDeviationFromAvg,
        int lowerDeviationFromAvg,
        int averageTemperature
    )
    public {
        thisSwap = WeatherSwap({
        indemnity : indemnity,
        remainingIndemnity : indemnity,
        minimumIndemnity : minimumIndemnity,
        premium : 0,
        contractStartDate : contractStartDate,
        contractEndDate : contractEndDate,
        upperDeviationFromAvg : upperDeviationFromAvg,
        lowerDeviationFromAvg : lowerDeviationFromAvg,
        averageTemperature : averageTemperature,
        region : region,
        status : 0,
        currentBalance :0 ,
        premiumToken : _premiumToken,
        indemnityToken : _indemnityToken
        });
        emitInformation();
    }

    function emitInformation() public {
        emit SwapCreation(
            thisSwap.contractStartDate,
            thisSwap.contractEndDate,
            thisSwap.premium,
            thisSwap.indemnity,
            thisSwap.remainingIndemnity,
            thisSwap.minimumIndemnity,
            thisSwap.region,
            thisSwap.upperDeviationFromAvg,
            thisSwap.lowerDeviationFromAvg,
            thisSwap.averageTemperature,
            thisSwap.status,
            address(this).balance,
            _premiumToken,
            _indemnityToken
        );
    }

    function buyPremium(uint256 amount_ )  payable external {
        TempSwap(_premiumToken).transfer( msg.sender, amount_);
        //payable(address (this)).transfer(msg.value);
        emitInformation();

    }

    function processEndOfContractPayments() internal {
        uint256 paymentToCustomer = thisSwap.indemnity - thisSwap.remainingIndemnity + (thisSwap.premium * 90 / 100);
        uint256 paymentToTenki = address(this).balance - paymentToCustomer;
        _premiumTokenPrice = 0;
        _indemnityTokenPrice = paymentToCustomer  / TempSwap(_indemnityToken).totalSupply();
        payable( tenkiCompanyAccount).transfer( paymentToTenki );

    }

    function processNormalEndPayment() internal {
        uint256 paymentToCustomer = thisSwap.indemnity - thisSwap.remainingIndemnity + (thisSwap.premium * 90 / 100);
        uint256 paymentToTenki = address(this).balance - paymentToCustomer;
        _indemnityTokenPrice = 0;
        _premiumTokenPrice = paymentToCustomer  / TempSwap(_premiumToken).totalSupply();
        payable( tenkiCompanyAccount).transfer( paymentToTenki );
    }


//    receive() payable external {
//        if ( thisSwap.status == 0 ){
//            processPremiumPayment ();
//        } else if ( thisSwap.status == 1){
//            processPremiumPayment ();
//        } else if (thisSwap.status == 2){
//            processIndemnityPayment();
//        } else if ( thisSwap.status == 3) {
//            processIndemnityPayment();
//        }else if ( thisSwap.status == 4) {
//            processIndemnityPayment();
//        }
//        else
//        {
//            revert("No more payments are accepted");
//        }
//        emitInformation();
//    }

    function endTempSwapNormally() public payable {
        require(msg.sender == tenkiCompanyAccount);
        thisSwap.status = 6;
        processEndOfContractPayments();
        emitInformation();
    }

    function catastrophicEventHappened() public payable {
        require(msg.sender == tenkiCompanyAccount);
        thisSwap.status = 5;
        processEndOfContractPayments();
        emitInformation();
    }
}