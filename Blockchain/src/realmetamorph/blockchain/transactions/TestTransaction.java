package realmetamorph.blockchain.transactions;

public class TestTransaction implements ITransaction{


    @Override
    public int getType() {
        return 0;
    }

    @Override
    public String getPublicKey() {
        return null;
    }

    @Override
    public void parseData(byte[] data) {

    }

    @Override
    public byte[] getData() {
        return new byte[0];
    }


}
