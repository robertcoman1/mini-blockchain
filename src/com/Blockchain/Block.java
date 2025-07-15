package com.Blockchain;
import java.util.ArrayList;
import java.util.Date;

public class Block {
    public String hash;
    public String merkleRoot;
    public String previousHash;
    public ArrayList<Transaction> transactions = new ArrayList<Transaction>();
    private long timestamp;
    private int nonce;

    public Block(String previousHash) {
        this.previousHash = previousHash;
        this.timestamp = new Date().getTime();
        this.hash = calculateHash();
    }

    public String calculateHash() {
        return helper.applySha256(previousHash + Long.toString(timestamp) + Integer.toString(nonce) + merkleRoot);
    }

    public void mineBlock(int difficulty) {
        merkleRoot = helper.getMerkleRoot(transactions);
        StringBuilder target = new StringBuilder();

        for (int i = 0 ; i < difficulty ; i++)
            target.append("0");
        while (!hash.substring(0, difficulty).equals(target.toString())) {
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Block minded :)" + hash);
    }
    
    public boolean addTransaction(Transaction transaction) {
        if (transaction == null)
            return false;
        
        if((previousHash != "0")) {
            if((!transaction.processTransaction())) {
                System.out.println("Transaction failed to process. Discarded.");
                return false;
            }
        }
        transactions.add(transaction);
        System.out.println("Transaction Successfully added to Block");

        return true;
    }
}