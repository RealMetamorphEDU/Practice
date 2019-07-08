package realmetamorph.blockchain.transactions;

public class TestTransaction implements ITransaction{


    @Override
    public int getType() {
        return 0;
    }

    @Override
    public void parseData(byte[] data) {

    }

    @Override
    public byte[] getData() {
        return new byte[]{12, 89, 5, 4, 78};
    }


}
