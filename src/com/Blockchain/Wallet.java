package com.Blockchain;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public class Wallet {
    public PrivateKey privateKey;
    public PublicKey publicKey;

    public HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();

    public Wallet() {
        generateKeyPair();
    }

    public float getBalance() {
        float balance = 0;

        for (Map.Entry<String, TransactionOutput> entry : Main.UTXOs.entrySet()) {

            if (entry.getValue().isMine(publicKey)) {
                balance += entry.getValue().value;
                UTXOs.put(entry.getValue().id, entry.getValue());
            }

        }
        
        return balance;
    }

    public Transaction sendFunds(PublicKey receiverKey, float value) {

        if(getBalance() < value) {
			System.out.println("Not Enough funds to send transaction. Transaction Discarded.");
			return null;
		}

        ArrayList<TransactionInput> inputs = new ArrayList<>();

        float total = 0;
        for (Map.Entry<String, TransactionOutput> entry : UTXOs.entrySet()) {
            TransactionOutput output = entry.getValue();
            total += output.value;
            inputs.add(new TransactionInput(output.id));
            if(total > value)
                break;
        }
        Transaction newTransaction = new Transaction(publicKey, receiverKey, value, inputs);
        newTransaction.generateSignature(privateKey);
        
        for(TransactionInput input : inputs){
			UTXOs.remove(input.transactionOutputId);
		}

        return newTransaction;
    }

    public void generateKeyPair() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
			// Initialize the key generator and generate a KeyPair
			keyGen.initialize(ecSpec, random);   //256 bytes provides an acceptable security level
	        	KeyPair keyPair = keyGen.generateKeyPair();
	        	// Set the public and private keys from the keyPair
	        	privateKey = keyPair.getPrivate();
	        	publicKey = keyPair.getPublic();
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
