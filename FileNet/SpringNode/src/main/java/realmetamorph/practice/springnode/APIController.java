/*******************************************************************************
 * Группа: БВТ1702.
 * Студент: Тимчук А.В.
 * Создано: 18.8.2019.
 * Copyright (c) 2019.
 ******************************************************************************/

package realmetamorph.practice.springnode;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.BsonBinary;
import org.bson.BsonDocument;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import realmetamorph.blockchain.Blockchain;
import realmetamorph.blockchain.block.Block;
import realmetamorph.blockchain.callbacks.*;
import realmetamorph.blockchain.network.INetMonitor;
import realmetamorph.blockchain.transactions.ITransaction;
import realmetamorph.blockchain.transactions.SignedTransaction;
import realmetamorph.practice.springnode.transactions.AddFileTransaction;
import realmetamorph.practice.springnode.transactions.DeleteFileTransaction;
import realmetamorph.practice.springnode.transactions.ReplaceFileTransaction;
import realmetamorph.practice.springnode.transactions.ShareFileTransaction;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

@RestControllerAdvice
@RequestMapping("/api")
public class APIController implements INetMonitor {

    private static final String SEED = "Spring node No 1 in blockchain! HAHCUEKAHKVMKFEO:ASLKO:FKJUTYBVNSDF";

    @Autowired
    private MongoTemplate mongo;
    @Autowired
    private GridFsTemplate gridFsTemplate;
    private FileMonitor fileMonitor;

    private TakeNewBlockCallback takeNewBlockCallback; // For the future, not used now
    private AskNewBlockCallback askNewBlockCallback;
    private AskHeightCallback askHeightCallback;
    private AskBlockCallback askBlockCallback;
    private TakeNewTransactionCallback takeNewTransactionCallback;

    private int takenTransactions;
    private Timer timer;

    @PostConstruct
    private void init() {
        try {
            fileMonitor = new FileMonitor(mongo.getDb());
            takenTransactions = 0;
            Blockchain blockchain = new Blockchain(Blockchain.WorkMode.NODE_MODE, fileMonitor, this, new BlockGenerator(), Blockchain.createKeys(SEED));
            Blockchain.registerTransaction(AddFileTransaction.class);
            Blockchain.registerTransaction(ReplaceFileTransaction.class);
            Blockchain.registerTransaction(DeleteFileTransaction.class);
            Blockchain.registerTransaction(ShareFileTransaction.class);
            blockchain.start();
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException ignored) {
        }
    }

    @GetMapping("/block/height/")
    @ResponseBody
    public String getBlockHeight() {
        int height = askHeightCallback.askHeight();
        return String.format("{\"height\":%d}", height);
    }

    @GetMapping("/block/all/{id}")
    @ResponseBody
    public String getBlock(@PathVariable("id") int id) {
        Block block = askBlockCallback.askBlock(id, false);
        if (block == null)
            return "{\"error\":\"Not exist.\"}";
        BsonBinary binary = new BsonBinary(block.getBlockData());
        return new BsonDocument("block_data", binary).toJson();
    }

    @GetMapping("/block/header/{id}")
    @ResponseBody
    public String getBlockHeader(@PathVariable("id") int id) {
        Block block = askBlockCallback.askBlock(id, true);
        if (block == null)
            return "{\"error\":\"Not exist.\"}";
        BsonBinary binary = new BsonBinary(block.getBlockData());
        return new BsonDocument("block_header", binary).toJson();
    }

    @PostMapping(value = "/transaction")
    @ResponseBody
    public ResponseEntity<String> putTransaction(@RequestParam("transaction") String transaction, @RequestParam(value = "file", required = false) MultipartFile file) {
        BsonDocument document = BsonDocument.parse(transaction);
        BsonBinary binary = document.getBinary("data");
        SignedTransaction signedTransaction;
        try {
            signedTransaction = new SignedTransaction(binary.getData());
            if (!signedTransaction.isValid() && !fileMonitor.validator(signedTransaction))
                return new ResponseEntity<>("{\"error\":\"Invalid transaction.\"}", HttpStatus.BAD_REQUEST);
        } catch (NoSuchAlgorithmException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | InvalidKeySpecException | InvalidKeyException | SignatureException e) {
            return new ResponseEntity<>("{\"error\":\"Bad transaction.\"}", HttpStatus.BAD_REQUEST);
        }
        ITransaction iTransaction = signedTransaction.getTransaction();
        if (iTransaction == null)
            return new ResponseEntity<>("{\"error\":\"Bad transaction.\"}", HttpStatus.BAD_REQUEST);
        switch (iTransaction.getType()) {
            case 0: {
                AddFileTransaction addFileTransaction = (AddFileTransaction) iTransaction;
                if (!fileMonitor.validAddOperation(signedTransaction.getReceiverKey(), Blockchain.bytes2hex(signedTransaction.getPublicKey().getEncoded()), addFileTransaction.getFilename()))
                    return new ResponseEntity<>("{\"error\":\"Invalid operation.\"}", HttpStatus.BAD_REQUEST);
                if (file == null || file.isEmpty())
                    return new ResponseEntity<>("{\"error\":\"No file\"}", HttpStatus.BAD_REQUEST);
                try {
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    digest.update(file.getName().getBytes(StandardCharsets.UTF_8));
                    InputStream inputStream = file.getInputStream();
                    while (inputStream.available() > 0) {
                        int len = Math.min(1024, inputStream.available());
                        byte[] bytes = new byte[len];
                        len = inputStream.read(bytes);
                        digest.update(bytes, 0, len);
                    }
                    String sha = Blockchain.bytes2hex(digest.digest(digest.digest()));
                    if (!Objects.equals(file.getName(), addFileTransaction.getFilename()) && !Objects.equals(addFileTransaction.getSha(), sha))
                        return new ResponseEntity<>("{\"error\":\"Invalid file info.\"}", HttpStatus.BAD_REQUEST);
                    if (!takeNewTransactionCallback.takeNewTransaction(signedTransaction))
                        return new ResponseEntity<>("{\"error\":\"Invalid transaction.\"}", HttpStatus.BAD_REQUEST);
                    takenTransactions++;
                    Document metadata = new Document();
                    metadata.put("user_key", Blockchain.bytes2hex(signedTransaction.getPublicKey().getEncoded()));
                    metadata.put("content_type", file.getContentType());
                    metadata.put("transaction_date", addFileTransaction.getDate().getTime());
                    metadata.put("original_name", file.getOriginalFilename());
                    try {
                        gridFsTemplate.store(file.getInputStream(), file.getName(), metadata);
                        return new ResponseEntity<>("{\"status\":\"The file is uploaded\"}", HttpStatus.CREATED);
                    } catch (Exception e) {
                        return new ResponseEntity<>("{\"error\":\"The file is not uploaded.\"}", HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | IOException e) {
                    return new ResponseEntity<>("{\"error\":\"Bad transaction.\"}", HttpStatus.BAD_REQUEST);
                }
            }
            case 1: {
                ReplaceFileTransaction replaceFileTransaction = (ReplaceFileTransaction) iTransaction;
                if (!fileMonitor.validReplaceOperation(signedTransaction.getReceiverKey(), Blockchain.bytes2hex(signedTransaction.getPublicKey().getEncoded()), replaceFileTransaction.getFilename()))
                    return new ResponseEntity<>("{\"error\":\"Invalid operation.\"}", HttpStatus.BAD_REQUEST);
                if (file == null || file.isEmpty())
                    return new ResponseEntity<>("{\"error\":\"No file\"}", HttpStatus.BAD_REQUEST);
                try {
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    digest.update(file.getName().getBytes(StandardCharsets.UTF_8));
                    InputStream inputStream = file.getInputStream();
                    while (inputStream.available() > 0) {
                        int len = Math.min(1024, inputStream.available());
                        byte[] bytes = new byte[len];
                        len = inputStream.read(bytes);
                        digest.update(bytes, 0, len);
                    }
                    String sha = Blockchain.bytes2hex(digest.digest(digest.digest()));
                    if (!Objects.equals(file.getName(), replaceFileTransaction.getFilename()) && !Objects.equals(replaceFileTransaction.getSha(), sha))
                        return new ResponseEntity<>("{\"error\":\"Invalid file info.\"}", HttpStatus.BAD_REQUEST);

                    Query query = new Query();
                    query.addCriteria(GridFsCriteria.whereFilename().is(replaceFileTransaction.getFilename()));
                    query.addCriteria(GridFsCriteria.whereMetaData("user_key").is(signedTransaction.getReceiverKey()));
                    GridFSFile gridFSFile = gridFsTemplate.findOne(query);
                    if (gridFSFile == null)
                        return new ResponseEntity<>("{\"error\":\"File not found.\"}", HttpStatus.BAD_REQUEST);
                    if (!takeNewTransactionCallback.takeNewTransaction(signedTransaction))
                        return new ResponseEntity<>("{\"error\":\"Invalid transaction.\"}", HttpStatus.BAD_REQUEST);
                    takenTransactions++;
                    Document oldMetadata = gridFSFile.getMetadata();
                    Document metadata = new Document();
                    metadata.put("user_key", oldMetadata.get("user_key"));
                    metadata.put("content_type", file.getContentType());
                    metadata.put("transaction_date", replaceFileTransaction.getDate().getTime());
                    metadata.put("original_name", file.getOriginalFilename());
                    gridFsTemplate.delete(query);
                    try {
                        gridFsTemplate.store(file.getInputStream(), replaceFileTransaction.getFilename(), metadata);
                        return new ResponseEntity<>("{\"status\":\"The file is replaced.\"}", HttpStatus.CREATED);
                    } catch (Exception e) {
                        return new ResponseEntity<>("{\"error\":\"The file is not replaced.\"}", HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | IOException e) {
                    return new ResponseEntity<>("{\"error\":\"Bad transaction.\"}", HttpStatus.BAD_REQUEST);
                }
            }
            case 2: {
                DeleteFileTransaction deleteFileTransaction = (DeleteFileTransaction) iTransaction;
                if (!fileMonitor.validDeleteOperation(signedTransaction.getReceiverKey(), Blockchain.bytes2hex(signedTransaction.getPublicKey().getEncoded()), deleteFileTransaction.getFilename()))
                    return new ResponseEntity<>("{\"error\":\"Invalid operation.\"}", HttpStatus.BAD_REQUEST);
                try {
                    Query query = new Query();
                    query.addCriteria(GridFsCriteria.whereFilename().is(deleteFileTransaction.getFilename()));
                    query.addCriteria(GridFsCriteria.whereMetaData("user_key").is(signedTransaction.getReceiverKey()));
                    if (!takeNewTransactionCallback.takeNewTransaction(signedTransaction))
                        return new ResponseEntity<>("{\"error\":\"Invalid transaction.\"}", HttpStatus.BAD_REQUEST);
                    takenTransactions++;
                    gridFsTemplate.delete(query);
                    return new ResponseEntity<>("{\"status\":\"Deleted\"}", HttpStatus.BAD_REQUEST);
                } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
                    return new ResponseEntity<>("{\"error\":\"Bad transaction.\"}", HttpStatus.BAD_REQUEST);
                }
            }
            case 3: {
                ShareFileTransaction shareFileTransaction = (ShareFileTransaction) iTransaction;
                byte mode = fileMonitor.getShareMode(signedTransaction.getReceiverKey(), Blockchain.bytes2hex(signedTransaction.getPublicKey().getEncoded()), shareFileTransaction.getFilename());
                if (mode == -1 || !fileMonitor.validShareMode(mode, shareFileTransaction.getMode()))
                    return new ResponseEntity<>("{\"error\":\"Invalid operation.\"}", HttpStatus.BAD_REQUEST);
                try {
                    if (!takeNewTransactionCallback.takeNewTransaction(signedTransaction))
                        return new ResponseEntity<>("{\"error\":\"Invalid transaction.\"}", HttpStatus.BAD_REQUEST);
                    takenTransactions++;
                    return new ResponseEntity<>("{\"status\":\"Shared\"}", HttpStatus.BAD_REQUEST);
                } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
                    return new ResponseEntity<>("{\"error\":\"Bad transaction.\"}", HttpStatus.BAD_REQUEST);
                }
            }
            default:
                return new ResponseEntity<>("{\"error\":\"Bad transaction.\"}", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/file/get")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile(@RequestParam(value = "filename") String filename, @RequestParam(value = "keeper_key") String keeperKey, @RequestParam(value = "key") String askedKey, @RequestParam(value = "sign") String sign) {
        try {
            byte[] filenameByte = filename.getBytes(StandardCharsets.UTF_8);
            byte[] keyKeeperByte = Blockchain.hex2bytes(keeperKey, false);
            byte[] keyByte = Blockchain.hex2bytes(askedKey, false);
            try {
                KeyFactory keyFactory = KeyFactory.getInstance("EC");
                EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(keyByte);
                PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                digest.update(filenameByte);
                digest.update(keyKeeperByte);
                digest.update(keyByte);
                String sha = Blockchain.bytes2hex(digest.digest(digest.digest()));
                if (!Blockchain.checkSignature(publicKey, sign, sha) || !fileMonitor.validFileGetOperation(keeperKey, askedKey, filename)) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            } catch (NoSuchAlgorithmException e) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (SignatureException | InvalidKeyException | InvalidKeySpecException e) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            Query query = new Query();
            query.addCriteria(GridFsCriteria.whereFilename().is(filename));
            query.addCriteria(GridFsCriteria.whereMetaData("user_key").is(keeperKey));
            GridFSFile fsFile = gridFsTemplate.findOne(query);
            if (fsFile == null)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            GridFsResource resource = gridFsTemplate.getResource(fsFile);
            if (resource == null)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            try {
                HttpHeaders headers = new HttpHeaders();
                String type = fsFile.getMetadata().getString("content_type");
                if (type != null)
                    headers.setContentType(MediaType.parseMediaType(type));
                headers.setContentLength(fsFile.getLength());
                return new ResponseEntity<>(new InputStreamResource(resource.getInputStream()), headers, HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception exception) {
        System.out.println(exception.getMessage());
        return exception.getMessage();
    }

    @Override
    public void start(Blockchain.WorkMode workMode) {
        timer = new Timer("Generator", true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (takenTransactions > 5) {
                    Block block = askNewBlockCallback.askNewBlock(takenTransactions);
                    if (block != null)
                        takenTransactions -= block.getTransactionsCount();
                }
            }
        }, 30000, 30000);
    }

    @Override
    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public Block getBlock(int i, boolean b) {
        // ignored
        return null;
    }

    @Override
    public int getHeight() {
        // ignored
        return 0;
    }

    @Override
    public ArrayList<SignedTransaction> getTransactionsByPublicKey(String s) {
        // ignored
        return null;
    }

    @Override
    public ArrayList<SignedTransaction> getTransactionsByType(int i) {
        // ignored
        return null;
    }

    @Override
    public void sendTransaction(SignedTransaction signedTransaction) {
        // ignored
    }

    @Override
    public void setCallbackTakeNewTransaction(TakeNewTransactionCallback takeNewTransactionCallback) {
        this.takeNewTransactionCallback = takeNewTransactionCallback;
    }

    @Override
    public void setCallbackAskBlock(AskBlockCallback askBlockCallback) {
        this.askBlockCallback = askBlockCallback;
    }

    @Override
    public void setCallbackAskHeight(AskHeightCallback askHeightCallback) {
        this.askHeightCallback = askHeightCallback;
    }

    @Override
    public void setCallbackAskNewBlock(AskNewBlockCallback askNewBlockCallback) {
        this.askNewBlockCallback = askNewBlockCallback;
    }

    @Override
    public void setCallbackTakeNewBlock(TakeNewBlockCallback takeNewBlockCallback) {
        // For the future
        this.takeNewBlockCallback = takeNewBlockCallback;
    }
}
