# Mini Blockchain

This is a simple implementation of a blockchain in Java.  
It demonstrates the basic concepts of how a blockchain works, including:

- **Blocks**: Each block stores a set of transactions.
- **Transactions**: Represent the transfer of value between wallets.
- **Wallets**: Each wallet has a public/private key pair used to sign transactions.
- **Chain Validation**: Each block is linked to the previous one through a cryptographic hash, ensuring data integrity.

## Features
✅ Create wallets and sign transactions  
✅ Add transactions to new blocks  
✅ Link blocks together to form a chain  
✅ Validate the blockchain to detect tampering

## How to Build
In the root of the project:
    javac -cp "lib/*" -d out $(find src -name "*.java")

## How to Run
    java -cp "out:lib/*" com.Blockchain.Main

Feel free to explore, modify, and learn how transactions are recorded in blocks and how the chain ensures security!