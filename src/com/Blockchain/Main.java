package com.Blockchain;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.GsonBuilder;

public class Main {
    static ArrayList<Block> blockchain = new ArrayList<>();
    public static HashMap<String, TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>(); //list of all unspent transactions. 
    public static int difficulty = 5;
    public static Wallet walletA;
	public static Wallet walletB;
    public static Transaction genesisTransaction;
    public static float minimumTransaction = 1;
    public static void main(String[] args) {    
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        
        walletA = new Wallet();
        walletB = new Wallet();
        Wallet coinbase = new Wallet();
        //Testing 
        genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100, null);
        genesisTransaction.generateSignature(coinbase.privateKey);
        genesisTransaction.transactionId = "0";

        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.receiverKey, genesisTransaction.value, genesisTransaction.transactionId));
        UTXOs.put(genesisTransaction.outputs.getFirst().id, genesisTransaction.outputs.getFirst());

        System.out.println("Creating and Mining Genesis block... ");
        Block genesis = new Block("0");
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);

        Block block1 = new Block(genesis.hash);
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("\nWalletA is Attempting to send funds (40) to WalletB...");
		block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
		addBlock(block1);
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletB's balance is: " + walletB.getBalance());
		
		Block block2 = new Block(block1.hash);
		System.out.println("\nWalletA Attempting to send more funds (1000) than it has...");
		block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
		addBlock(block2);
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletB's balance is: " + walletB.getBalance());
		
		Block block3 = new Block(block2.hash);
		System.out.println("\nWalletB is Attempting to send funds (20) to WalletA...");
		block3.addTransaction(walletB.sendFunds(walletA.publicKey, 20));
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletB's balance is: " + walletB.getBalance());
		addBlock(block3);

		isChainValid();
    }
    public static boolean isChainValid() {
        Block currentBlock;
        Block prevBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); //a temporary working list of unspent transactions at a given block state.
		tempUTXOs.put(genesisTransaction.outputs.getFirst().id, genesisTransaction.outputs.getFirst());

        for (int i = 1 ; i < blockchain.size() ; i++) {
            currentBlock = blockchain.get(i);
            prevBlock = blockchain.get(i - 1);

            if (!currentBlock.previousHash.equals(prevBlock.hash)) {
                System.out.println("Previous Hashes not equal");
                return false;
            }

            if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
                System.out.println("Current hash not equal");
                return false;
            }

            if (!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
                System.out.println("Block hasnt been mined");
                return false;
            }

            TransactionOutput tempOutput;
            for (int j = 0 ; j < currentBlock.transactions.size() ; j++) {
                Transaction currTransaction = currentBlock.transactions.get(j);
                
                if(!currTransaction.verifySignature()) {
					System.out.println("#Signature on Transaction(" + j + ") is Invalid");
					return false; 
				}

                if (currTransaction.getInputsValue() != currTransaction.getOutputValue()) {
                    System.out.println("#Inputs are note equal to outputs on Transaction(" + j + ")");
					return false;
                }

                for (TransactionInput input : currTransaction.inputs) {
                    tempOutput = tempUTXOs.get(input.transactionOutputId);

                    if(tempOutput == null) {
						System.out.println("#Referenced input on Transaction(" + j + ") is Missing");
						return false;
					}

                    if(input.UTXO.value != tempOutput.value) {
						System.out.println("#Referenced input Transaction(" + j + ") value is Invalid");
						return false;
					}

                    tempUTXOs.remove(input.transactionOutputId);
                }

                for(TransactionOutput output: currTransaction.outputs) {
					tempUTXOs.put(output.id, output);
				}
                
                if( currTransaction.outputs.get(0).receiver != currTransaction.receiverKey) {
					System.out.println("#Transaction(" + j + ") output reciepient is not who it should be");
					return false;
				}
				if( currTransaction.outputs.get(1).receiver != currTransaction.senderKey) {
					System.out.println("#Transaction(" + j + ") output 'change' is not sender.");
					return false;
				}
            }
        }
        System.out.println("BlockChain is valid");
        return true;
    }
    public static void addBlock(Block newBlock) {
		newBlock.mineBlock(difficulty);
		blockchain.add(newBlock);
	}
}