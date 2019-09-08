import realmetamorph.blockchain.Blockchain;
import realmetamorph.blockchain.block.Block;
import realmetamorph.blockchain.transactions.SignedTransaction;
import transactions.AddFileTransaction;
import transactions.DeleteFileTransaction;
import transactions.ReplaceFileTransaction;
import transactions.ShareFileTransaction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

/*******************************************************************************
 * Группа: БВТ1702.
 * Студент: Тимчук А.В.
 * Создано: 1.9.2019.
 * Copyright (c) 2019.
 ******************************************************************************/

class FilesMonitor {

    private NetMonitor net;
    private int height;

    FilesMonitor(NetMonitor net) {
        this.net = net;
    }

    void update() {
        File dir = new File("blockchain");
        dir.mkdir();
        height = net.getHeight();
        for (int i = 1; i <= height; i++) {
            File blockFile = new File(dir, "block_" + i + ".blk");
            if (blockFile.exists())
                continue;
            Block block = net.getBlock(i, false);
            try {
                FileOutputStream stream = new FileOutputStream(blockFile);
                stream.write(block.getBlockData());
                stream.close();
            } catch (IOException ignored) {
            }
        }
    }

    void printFiles(PublicKey key) {
        update();
        File dir = new File("blockchain");
        int _height = height;
        ArrayList<SignedTransaction> add = new ArrayList<>();
        ArrayList<SignedTransaction> replace = new ArrayList<>();
        ArrayList<SignedTransaction> delete = new ArrayList<>();
        ArrayList<SignedTransaction> share = new ArrayList<>();
        while (_height != 0) {
            File blockFile = new File(dir, "block_" + _height + ".blk");
            Block block = null;
            if (!blockFile.exists())
                continue;
            try {
                FileInputStream stream = new FileInputStream(blockFile);
                byte[] data = new byte[stream.available()];
                stream.read(data);
                block = new Block(data, false);
            } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException ignored) {
            }
            if (block != null)
                for (int i = 0; i < block.getTransactionsCount(); i++) {
                    SignedTransaction signedTransaction = block.getTransaction(i);
                    switch (signedTransaction.getTransaction().getType()) {
                        case 0:
                            add.add(signedTransaction);
                            break;
                        case 1:
                            replace.add(signedTransaction);
                            break;
                        case 2:
                            delete.add(signedTransaction);
                            break;
                        case 3:
                            share.add(signedTransaction);
                            break;
                    }
                }
            _height--;
        }
        add.sort((o1, o2) -> {
            AddFileTransaction add1 = (AddFileTransaction) o1.getTransaction();
            AddFileTransaction add2 = (AddFileTransaction) o2.getTransaction();
            return add1.getDate().compareTo(add2.getDate());
        });
        replace.sort((o1, o2) -> {
            ReplaceFileTransaction replace1 = (ReplaceFileTransaction) o1.getTransaction();
            ReplaceFileTransaction replace2 = (ReplaceFileTransaction) o2.getTransaction();
            return replace1.getDate().compareTo(replace2.getDate());
        });
        delete.sort((o1, o2) -> {
            ShareFileTransaction delete1 = (ShareFileTransaction) o1.getTransaction();
            ShareFileTransaction delete2 = (ShareFileTransaction) o2.getTransaction();
            return delete1.getDate().compareTo(delete2.getDate());
        });
        share.sort((o1, o2) -> {
            ShareFileTransaction share1 = (ShareFileTransaction) o1.getTransaction();
            ShareFileTransaction share2 = (ShareFileTransaction) o2.getTransaction();
            return share1.getDate().compareTo(share2.getDate());
        });

        for (SignedTransaction signedTransaction : delete) {
            DeleteFileTransaction deleteFileTransaction = (DeleteFileTransaction) signedTransaction.getTransaction();
        }
        for (SignedTransaction signedTransaction : share) {
            ShareFileTransaction shareFileTransaction = (ShareFileTransaction) signedTransaction.getTransaction();
        }
        for (SignedTransaction signedTransaction : add) {
            AddFileTransaction addFileTransaction = (AddFileTransaction) signedTransaction.getTransaction();
        }
    }

    void printFiles(String key) {
        byte[] pkBytes = Blockchain.hex2bytes(key, false);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(pkBytes);
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            printFiles(publicKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.out.println("Invalid key!");
        }
    }
}
