package com.Blockchain;

import java.security.*;
import java.util.ArrayList;

public class Transaction {
    public String transactionId;
    public PublicKey senderKey;
    public PublicKey receiverKey;
    public float value;
    public byte[] signature;

    public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
	public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();

    private static int sequence = 0; 

    public Transaction(PublicKey senderKey, PublicKey receiverKey, float value, ArrayList<TransactionInput> inputs) {
        this.senderKey = senderKey;
        this.receiverKey = receiverKey;
        this.value = value;
        this.inputs = inputs;
    }

    private String calulateHash() {
        sequence++;
        return helper.applySha256(helper.getStringFromKey(senderKey) +
                                helper.getStringFromKey(receiverKey) +
                                Float.toString(value) + sequence);
    }

    public void generateSignature(PrivateKey privateKey) {
        String data = helper.getStringFromKey(senderKey) + helper.getStringFromKey(receiverKey) + Float.toString(value);
        signature = helper.applyECDSASig(privateKey, data);
    }

    public boolean verifySignature() {
        String data = helper.getStringFromKey(senderKey) + helper.getStringFromKey(receiverKey) + Float.toString(value);
        return helper.verifyECDSASig(senderKey, data, signature);
    }

    public boolean processTransaction() {
        if (!verifySignature()) {
            System.out.println("Transaction failed to verify");
            return false;
        }

        for (TransactionInput in : inputs) {
            in.UTXO = Main.UTXOs.get(in.transactionOutputId);
        }

        if (getInputsValue() < Main.minimumTransaction) {
            System.out.println("Transaction Inputs to small: " + getInputsValue());
			return false;
        }

        float leftOver = getInputsValue() - value;
        transactionId = calulateHash();
        outputs.add(new TransactionOutput(receiverKey, value, transactionId));
        outputs.add(new TransactionOutput(senderKey, leftOver, transactionId));

        for (TransactionOutput out : outputs) {
            Main.UTXOs.put(out.id, out);
        }
        
        for (TransactionInput in : inputs) {
            if (in.UTXO == null)
                continue;
            Main.UTXOs.remove(in.UTXO.id);
        }

        return true;
    }
    public float getInputsValue() {
        float total = 0;

        for (TransactionInput in : inputs) {
            if(in.UTXO == null)
                continue;
            total += in.UTXO.value;
        }

        return total;
    }
    public float getOutputValue() {
        float total = 0;

        for (TransactionOutput out : outputs) {
            total += out.value;
        }

        return total;
    }
}
