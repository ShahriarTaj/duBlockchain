export COMPILE="solc --abi --bin --optimize  -o build --overwrite $1.sol"
echo $COMPILE
$COMPILE
export GENERATE="web3j generate solidity -b build/$1.bin -a build/$1.abi -o ../../java -p edu.duke.online.tenki.generated"
echo $GENERATE
$GENERATE
