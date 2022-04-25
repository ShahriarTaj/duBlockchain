// SPDX-License-Identifier: GPL-3.0
pragma solidity ^0.8.00;


contract DukeWeatherContract is ERC20Interface, Owned, SafeMath {
    string public symbol;
    string public  name;
    uint8 public decimals;
    uint public _totalSupply;

    mapping(address => uint) balances;
    mapping(address => mapping(address => uint)) allowed;

    struct WeatherSwap {

        uint contractStartDate;
        uint contractEndDate;
        uint256 premium;
        uint256 indemnity;
        uint256 remainingIndemnity;
        uint256 minimumIndemnity;
        uint256 currentBalance;
        string region;
        int upperDeviationFromAvg;
        int lowerDeviationFromAvg;
        int averageTemperature;
        address insuredAddress;
        int status;
    }

    struct Insurer {
        address insurer;
        uint256 amount;
    }

    WeatherSwap  thisSwap;
    Insurer [] insurers;
    address tenkiCompanyAccount = 0xEc10233C905e647fEd29144A4e411c1d7E311264;
    address insured;
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
        uint256 currentBalance,
        string region,
        int upperDeviationFromAvg,
        int lowerDeviationFromAvg,
        int averageTemperature,
        uint insurersCount,
        address insuredAddress,
        int status     // 0 created but not funded (no premium has been sent)
    // 1 Funded but minimum indemnity is not reached
    // 2 funded and insured
    // 3 ended with "event" happened and the protection buyer must be paid
    // 4 ended without the "event" happening, the risk buyer (insurers) must be paid

    );

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    constructor(string memory symbol_, uint quantity_ ) {
        //require(msg.sender == tenkiCompanyAccount);
        symbol = symbol_;
        name = "Test Duke Token ";
        decimals = 0;
        _totalSupply = quantity_;
        owner = 0xEc10233C905e647fEd29144A4e411c1d7E311264;
        //paste your public address
        balances[0xEc10233C905e647fEd29144A4e411c1d7E311264] = _totalSupply;
        //paste your public address again
        emit Transfer(address(0), 0xEc10233C905e647fEd29144A4e411c1d7E311264, _totalSupply);
    }

    // ------------------------------------------------------------------------
    // Total supply
    // ------------------------------------------------------------------------
    function totalSupply() public override view returns (uint) {
        return _totalSupply - balances[address(0)];
    }


    // ------------------------------------------------------------------------
    // Get the token balance for account tokenOwner
    // ------------------------------------------------------------------------
    function balanceOf(address tokenOwner) public override view returns (uint balance) {
        return balances[tokenOwner];
    }


    // ------------------------------------------------------------------------
    // Transfer the balance from token owner's account to to account
    // - Owner's account must have sufficient balance to transfer
    // - 0 value transfers are allowed
    // ------------------------------------------------------------------------
    function transfer(address to, uint tokens) public override returns (bool success) {
        balances[msg.sender] = safeSub(balances[msg.sender], tokens);
        balances[to] = safeAdd(balances[to], tokens);
        emit Transfer(msg.sender, to, tokens);
        return true;
    }


    // ------------------------------------------------------------------------
    // Token owner can approve for spender to transferFrom(...) tokens
    // from the token owner's account
    //
    // https://github.com/ethereum/EIPs/blob/master/EIPS/eip-20-token-standard.md
    // recommends that there are no checks for the approval double-spend attack
    // as this should be implemented in user interfaces
    // ------------------------------------------------------------------------
    function approve(address spender, uint tokens) public override returns (bool success) {
        allowed[msg.sender][spender] = tokens;
        emit Approval(msg.sender, spender, tokens);
        return true;
    }


    // ------------------------------------------------------------------------
    // Transfer tokens from the from account to the to account
    //
    // The calling account must already have sufficient tokens approve(...)-d
    // for spending from the from account and
    // - From account must have sufficient balance to transfer
    // - Spender must have sufficient allowance to transfer
    // - 0 value transfers are allowed
    // ------------------------------------------------------------------------
    function transferFrom(address from, address to, uint tokens) public override returns (bool success) {
        balances[from] = safeSub(balances[from], tokens);
        //the following line is suspect
        //allowed[from][from] = safeSub(allowed[from][from], tokens);
        balances[to] = safeAdd(balances[to], tokens);
        emit Transfer(from, to, tokens);
        return true;
    }


    // ------------------------------------------------------------------------
    // Returns the amount of tokens approved by the owner that can be
    // transferred to the spender's account
    // ------------------------------------------------------------------------
    function allowance(address tokenOwner, address spender) public override view returns (uint remaining) {
        return allowed[tokenOwner][spender];
    }


    // ------------------------------------------------------------------------
    // Token owner can approve for spender to transferFrom(...) tokens
    // from the token owner's account. The spender contract function
    // receiveApproval(...) is then executed
    // ------------------------------------------------------------------------
    function approveAndCall(address spender, uint tokens, bytes memory data) public returns (bool success) {
        allowed[msg.sender][spender] = tokens;
        emit Approval(msg.sender, spender, tokens);
        ApproveAndCallFallBack(spender).receiveApproval(msg.sender, tokens, address(this), data);
        return true;
    }


    function buyProtection(uint256 _amount) external {
        // When a person buys protection -- the insured sends us money which will applied to the premium
        // and will send us ether
        //        bool sent = payable( address (this)  ).send(_amount);
        //        require(sent, "Failure unable to send ether " );
        transferFrom({to: msg.sender, from: owner, tokens: _amount});
        emit Transfer({from : owner, to : msg.sender , tokens :  _amount});
    }

    receive() payable external {
        emit Transfer({from : msg.sender, to : address (this) , tokens :  msg.value});
    }

    // ------------------------------------------------------------------------
    // Owner can transfer out any accidentally sent ERC20 tokens
    // ------------------------------------------------------------------------
    function transferAnyERC20Token(address tokenAddress, uint tokens) public onlyOwner returns (bool success) {
        return ERC20Interface(tokenAddress).transfer(owner, tokens);
    }

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
        currentBalance : 0,
        premium : 0,
        contractStartDate : contractStartDate,
        contractEndDate : contractEndDate,
        upperDeviationFromAvg : upperDeviationFromAvg,
        lowerDeviationFromAvg : lowerDeviationFromAvg,
        averageTemperature : averageTemperature,
        region : region,
        insuredAddress : msg.sender,
        status : 0});
        insured = msg.sender;
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
            thisSwap.currentBalance,
            thisSwap.region,
            thisSwap.upperDeviationFromAvg,
            thisSwap.lowerDeviationFromAvg,
            thisSwap.averageTemperature,
            insurers.length,
            thisSwap.insuredAddress,
            thisSwap.status
        );
    }

    function buyProtection(uint256 amount_){
        thisSwap.premium += amount_;
        thisSwap.status = 1;
        transfer(msg.sender, amount_);

    }

    function setPremium  (uint256 amount_) public {
        thisSwap.premium = amount_;
        thisSwap.status = 1;

    }

    function setInsurer( address insurer_, uint256 amount_) public {
        Insurer memory insurer = Insurer({insurer : insurer_, amount : amount_});
        insurers.push(insurer);
        thisSwap.remainingIndemnity -= amount_;
        thisSwap.currentBalance += amount_;

        if (thisSwap.remainingIndemnity <= thisSwap.minimumIndemnity) {
            thisSwap.status = 2;
        }

    }

    function catastrophicEventHappened() public payable {
        require(msg.sender == tenkiCompanyAccount);
        thisSwap.status = 3;
        emitInformation();
    }
}