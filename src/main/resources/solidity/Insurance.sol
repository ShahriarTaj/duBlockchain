pragma solidity ^0.8.0;
// SPDX-License-Identifier: GPL-3.0
import "./openzeppelin-contracts/contracts/token/ERC20/ERC20.sol";

contract Insurance is ERC20 {

    uint256 public _price;
    address public _owner;
    address internal tenkiAddress = 0xEc10233C905e647fEd29144A4e411c1d7E311264; //this should be inserted into the constructor
    constructor(string memory name_, string memory symbol_, uint256 totalSupply_)
        ERC20(name_, symbol_) {
            _mint(msg.sender, totalSupply_);
            _price = 0;
            _owner = msg.sender;
        }

    function setPrice(uint256 newPrice_) external{
        _price = newPrice_;
    }
    function decimals() public view virtual override returns (uint8) {
        return 0;
    }
    function allowance(address owner, address spender) public view virtual override returns (uint256) {
        require (owner != address(0)  && spender != address (0) );
        return  10000e18;
    }


}