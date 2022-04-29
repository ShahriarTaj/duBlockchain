// SPDX-License-Identifier: GPL-3.0
pragma solidity ^0.8.00;

import "./Insurance.sol";


contract DukeWeatherContract {
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
        address insuredAddress;
        int status;
        uint256 currentBalance;
    }

    address private _indemnityProviders;
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
        address insuredAddress,
    // status with codes below
        int status,
    // 0 premium pledged  but not funded (no premium has been sent)
    // 1 premium funded
    // 2 indemnity pledged but pledges don't reach minimum
    // 3 indemnity minimum has been reached but not funded
    // 4 indemnity minimum is funded
    // 5 ended with "event" happened and the protection buyer must be paid
    // 6 ended without the "event" happening, the risk buyer (insurers) must be paid
        uint256 currentBalance,
        address _indemnityProviders

    );

    constructor(address premiumTokenAddress_, address indemnityTokenAddress_)  {
        require(msg.sender == tenkiCompanyAccount);
        _premiumTokenAddress = premiumTokenAddress_;
        _indemnityTokenAddress = indemnityTokenAddress_;
        _premiumTokenPrice = 0;
        _indemnityTokenPrice = 0;
    }
    address _premiumTokenAddress;
    address _indemnityTokenAddress;
    uint256 _premiumTokenPrice;
    uint256 _indemnityTokenPrice;

    function createNewInsuranceContract(
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
        insuredAddress : msg.sender,
        status : 0,
        currentBalance :0 });
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
            thisSwap.insuredAddress,
            thisSwap.status,
            address(this).balance,
            _indemnityProviders
        );
    }



    function pledgePremium( uint256 amount_) public {
        require(msg.sender == thisSwap.insuredAddress , "Only the insured can pledge premium" );
        thisSwap.premium += amount_;
        thisSwap.status = 1;
        emitInformation();
    }


    function processIndemnityPayment ()   internal   {
        require (
            (thisSwap.status == 2 || thisSwap.status == 3) ,
            "The payment to cover indemnity can only payed if there are enough pledges");
        require (thisSwap.insuredAddress != msg.sender, "The insured need no longer pay premium");
        require (tenkiCompanyAccount != msg.sender, "Tenki should not be paying");

        thisSwap.remainingIndemnity -= msg.value;
        _indemnityProviders = msg.sender;

        if ( thisSwap.remainingIndemnity <= thisSwap.minimumIndemnity){
            thisSwap.status = 4;
        }
        Insurance(_indemnityTokenAddress).transferFrom( { from : tenkiCompanyAccount ,
        to : payable(msg.sender),
        amount : msg.value / 1e18 });

    }
    function processPremiumPayment () internal {
        require( msg.sender == thisSwap.insuredAddress, "Only the insured can pay the premium");
        require (thisSwap.status == 1 || thisSwap.status == 0 ,  "The insured can only pay after pledging premium");


        //Move premium tokens to the insured
        Insurance(_premiumTokenAddress).transferFrom( { from : tenkiCompanyAccount ,
            to : payable(thisSwap.insuredAddress),
            amount : msg.value / 1e18 });

        thisSwap.status = 2;

    }

    function processEndOfContractPayments(address payable customerAddress) internal {

        uint256 paymentToCustomer = 95 * (thisSwap.indemnity - thisSwap.remainingIndemnity + thisSwap.premium) / 100;
        uint256 paymentToTenki = address(this).balance - paymentToCustomer;

        //Insurance(_premiumTokenAddress).setPrice( paymentToProtectionBuyer / Insurance(_premiumTokenAddress).totalSupply());


        payable( customerAddress ).transfer(paymentToCustomer);
        payable( tenkiCompanyAccount).transfer( paymentToTenki );

    }

    function processNormalEndPayment() internal {
        uint256 paymentToRiskBuyer = address (this).balance;// - 1e18;// / 100 * 90;
        payable( _indemnityProviders).transfer( paymentToRiskBuyer );
        payable( tenkiCompanyAccount).transfer( address (this).balance - paymentToRiskBuyer );
    }


    receive() payable external {
        if ( thisSwap.status == 0 ){
            processPremiumPayment ();
        } else if ( thisSwap.status == 1){
            processPremiumPayment ();
        } else if (thisSwap.status == 2){
            processIndemnityPayment();
        } else if ( thisSwap.status == 3) {
            processIndemnityPayment();
        }else if ( thisSwap.status == 4) {
            processIndemnityPayment();
        }
        else
        {
            revert("No more payments are accepted");
        }
        emitInformation();
    }

    function endInsuranceNormally() public payable {
        require(msg.sender == tenkiCompanyAccount);
        thisSwap.status = 6;
        processEndOfContractPayments(payable(_indemnityProviders));
        emitInformation();
    }

    function catastrophicEventHappened() public payable {
        require(msg.sender == tenkiCompanyAccount);
        thisSwap.status = 5;
        processEndOfContractPayments(payable(thisSwap.insuredAddress));
        emitInformation();
    }
}