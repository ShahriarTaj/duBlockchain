# Getting Started

###First compile the Solidity code
* Refer to web3j.io (http://docs.web3j.io/4.8.7/getting_started/deploy_interact_smart_contracts/)
####I use the example shah.sol
solc --abi --bin --optimize  -o build --overwrite shah.sol

###Then generate the java wrapper
web3j generate solidity -b build/TST.bin -a build/TST.abi -o ../../java -p edu.duke.online.tenki.generated

You can also run compileAndGenerate.sh -- Note that the File name and the contract name MUST be the same for compile and generate to work.


##Some useful geth console commands
funding an account
eth.sendTransaction({from:eth.coinbase, to:"0x6991381B245330A93B63bc6Be5C2a8d3D21F2A83", value: web3.toWei(1000, "ether")})