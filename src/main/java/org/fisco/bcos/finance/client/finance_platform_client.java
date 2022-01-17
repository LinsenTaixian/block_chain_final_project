package org.fisco.bcos.finance.client;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Properties;
import org.fisco.bcos.finance.contract.Finance_platform;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple1;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple3;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple4;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;


public class finance_platform_client {
    static Logger logger = LoggerFactory.getLogger(finance_platform_client.class);

    private BcosSDK bcosSDK;
    private Client client;
    private CryptoKeyPair cryptoKeyPair;

    public void initialize() throws Exception {
        @SuppressWarnings("resource")
        ApplicationContext context =
                new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        bcosSDK = context.getBean(BcosSDK.class);
        client = bcosSDK.getClient(1);
        cryptoKeyPair = client.getCryptoSuite().createKeyPair();
        client.getCryptoSuite().setCryptoKeyPair(cryptoKeyPair);
        logger.debug("create client for group1, account address is " + cryptoKeyPair.getAddress());
    }

    public void deployFinanceAndRecordAddr() {

        try {
            Finance_platform finance_platform = Finance_platform.deploy(client, cryptoKeyPair);
            System.out.println(
                    " deploy Finance success, contract address is " + finance_platform.getContractAddress());

            recordFinanceAddr(finance_platform.getContractAddress());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            System.out.println(" deploy Finance contract failed, error message is  " + e.getMessage());
        }
    }

    public void recordFinanceAddr(String address) throws FileNotFoundException, IOException {
        Properties prop = new Properties();
        prop.setProperty("address", address);
        final Resource contractResource = new ClassPathResource("contract.properties");
        FileOutputStream fileOutputStream = new FileOutputStream(contractResource.getFile());
        prop.store(fileOutputStream, "contract address");
    }

    public String loadFinanceAddr() throws Exception {
        // load Finance contact address from contract.properties
        Properties prop = new Properties();
        final Resource contractResource = new ClassPathResource("contract.properties");
        prop.load(contractResource.getInputStream());

        String contractAddress = prop.getProperty("address");
        if (contractAddress == null || contractAddress.trim().equals("")) {
            throw new Exception(" load Finance contract address failed, please deploy it first. ");
        }
        logger.info(" load Finance address from contract.properties, address is {}", contractAddress);
        return contractAddress;
    }

    public void queryFinanceAmount(String financeAccount) {
        try {
            String contractAddress = loadFinanceAddr();
            Finance_platform finance_platform = Finance_platform.load(contractAddress, client, cryptoKeyPair);
            Tuple2<Boolean, BigInteger> result = finance_platform.selectEntity(financeAccount);
            if (result.getValue1() == true) {
                System.out.printf(" finance_platform account %s, value %s \n", financeAccount, result.getValue2());
            } else {
                System.out.printf(" %s finance_platform account is not exist \n", financeAccount);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            logger.error(" queryFinanceAmount exception, error message is {}", e.getMessage());

            System.out.printf(" query finance_platform account failed, error message is %s\n", e.getMessage());
        }
    }

    public void queryTransaction(String id) {
        try {
            String contractAddress = loadFinanceAddr();
            Finance_platform finance_platform = Finance_platform.load(contractAddress, client, cryptoKeyPair);
            Tuple4<Boolean, String, String, BigInteger> result = finance_platform.selectTransaction(id);
            if (result.getValue1() == true) {
                System.out.printf(" transaction is id : %s, acc1 %s, acc1 %s, money %s \n", id, result.getValue2(), result.getValue3(), result.getValue4());
            } else {
                System.out.printf(" %s transaction is not exist \n", id);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            logger.error(" queryTranscation exception, error message is {}", e.getMessage());

            System.out.printf(" query Transcation failed, error message is %s\n", e.getMessage());
        }
    }

    public void registerFinanceAccount(String financeAccount, BigInteger amount) {
        try {
            String contractAddress = loadFinanceAddr();

            Finance_platform finance_platform = Finance_platform.load(contractAddress, client, cryptoKeyPair);
            TransactionReceipt receipt = finance_platform.register(financeAccount, amount);
            List<Finance_platform.RegisterEventEventResponse> response = finance_platform.getRegisterEventEvents(receipt);
            if (!response.isEmpty()) {
                if (response.get(0).ret == true) {
                    System.out.printf(
                            " register finance_platform account success => finance_platform: %s, value: %s \n", financeAccount, amount);
                } else {
                    System.out.printf(
                            " register finance_platform account failed, ret code is %s \n", response.get(0).ret.toString());
                }
            } else {
                System.out.println(" event log not found, maybe transaction not exec. ");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();

            logger.error(" registerFinanceAccount exception, error message is {}", e.getMessage());
            System.out.printf(" register finance_platform account failed, error message is %s\n", e.getMessage());
        }
    }

    public void transferFinance(String fromFinanceAccount, String toFinanceAccount, BigInteger amount) {
        try {
            String contractAddress = loadFinanceAddr();
            Finance_platform finance_platform = Finance_platform.load(contractAddress, client, cryptoKeyPair);
            TransactionReceipt receipt = finance_platform.transfer(fromFinanceAccount, toFinanceAccount, amount);
            List<Finance_platform.TransferEventEventResponse> response = finance_platform.getTransferEventEvents(receipt);
            if (!response.isEmpty()) {
                if (response.get(0).ret == true) {
                    System.out.printf(
                            " transfer success => from_finance_platform: %s, to_finance_platform: %s, amount: %s \n",
                            fromFinanceAccount, toFinanceAccount, amount);
                } else {
                    System.out.printf(
                            " transfer finance_platform account failed, ret code is %s \n", response.get(0).ret.toString());
                }
            } else {
                System.out.println(" event log not found, maybe transaction not exec. ");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();

            logger.error(" registerFinanceAccount exception, error message is {}", e.getMessage());
            System.out.printf(" register finance_platform account failed, error message is %s\n", e.getMessage());
        }
    }

    public void transactionUpload(String id, String acc1, String acc2, BigInteger money) {
        try {
            String contractAddress = loadFinanceAddr();
            Finance_platform finance_platform = Finance_platform.load(contractAddress, client, cryptoKeyPair);
            TransactionReceipt receipt = finance_platform.addTransaction(id, acc1, acc2,money);
            List<Finance_platform.AddTransactionEventEventResponse> response = finance_platform.getAddTransactionEventEvents(receipt);
            if (!response.isEmpty()) {
                if (response.get(0).ret == true) {
                    System.out.printf(
                            " transactionUpload success => id: %s, from_finance_platform: %s, to_finance_platform: %s, amount: %s \n",
                            id, acc1, acc2, money);
                } else {
                    System.out.printf(
                            " transferUpload failed, ret code is %s \n", response.get(0).ret.toString());
                }
            } else {
                System.out.println(" event log not found, maybe transaction not exec. ");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();

            logger.error(" transactionUpload exception, error message is {}", e.getMessage());
            System.out.printf(" transactionUpload failed, error message is %s\n", e.getMessage());
        }
    }

    public void transactionUpdate(String id, BigInteger money) {
        try {
            String contractAddress = loadFinanceAddr();
            Finance_platform finance_platform = Finance_platform.load(contractAddress, client, cryptoKeyPair);
            TransactionReceipt receipt = finance_platform.updateTransaction(id, money);
            List<Finance_platform.UpdateTransactionEventEventResponse> response = finance_platform.getUpdateTransactionEventEvents(receipt);
            if (!response.isEmpty()) {
                if (response.get(0).ret == true) {
                    System.out.printf(
                            " transactionUpdate success => id: %s,  amount: %s \n",
                            id, money);
                } else {
                    System.out.printf(
                            " transactionUpdate false, ret code is %s \n", response.get(0).ret.toString());
                }
            } else {
                System.out.println(" event log not found, maybe transaction not exec. ");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();

            logger.error(" transactionUpdate exception, error message is {}", e.getMessage());
            System.out.printf(" transactionUpdate failed, error message is %s\n", e.getMessage());
        }
    }

    public void splitFinanceTransaction(String old_id, String new_id, String new_acc, BigInteger money) {
        try {
            String contractAddress = loadFinanceAddr();
            Finance_platform finance_platform = Finance_platform.load(contractAddress, client, cryptoKeyPair);
            TransactionReceipt receipt = finance_platform.splitTransaction(old_id, new_id, new_acc, money);
            List<Finance_platform.SplitTransactionEventEventResponse> response = finance_platform.getSplitTransactionEventEvents(receipt);
            if (!response.isEmpty()) {
                if (response.get(0).ret == true) {
                    System.out.printf(
                            " splitFinanceTransaction success => old_id: %s, new_id: %s, new_acc: %s, amount: %s \n",
                            old_id, new_id, new_acc, money);
                } else {
                    System.out.printf(
                            " splitFinanceTransaction failed, ret code is %s \n", response.get(0).ret.toString());
                }
            } else {
                System.out.println(" event log not found, maybe transaction not exec. ");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();

            logger.error(" splitFinanceTransaction exception, error message is {}", e.getMessage());
            System.out.printf(" splitFinanceTransaction failed, error message is %s\n", e.getMessage());
        }
    }
    public void removeFinanceTransaction(String id) {
        try {
            String contractAddress = loadFinanceAddr();
            Finance_platform finance_platform = Finance_platform.load(contractAddress, client, cryptoKeyPair);
            TransactionReceipt receipt = finance_platform.removeTransaction(id);
            List<Finance_platform.RemoveTransactionEventEventResponse> response = finance_platform.getRemoveTransactionEventEvents(receipt);
            if (!response.isEmpty()) {
                if (response.get(0).ret == true) {
                    System.out.printf(
                            " removeFinanceTransaction success => id: %s \n", id);
                } else {
                    System.out.printf(
                            " removeFinanceTransaction false, ret code is %s \n", response.get(0).ret.toString());
                }
            } else {
                System.out.println(" event log not found, maybe transaction not exec. ");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();

            logger.error(" removeFinanceTransaction exception, error message is {}", e.getMessage());
            System.out.printf(" removeFinanceTransaction failed, error message is %s\n", e.getMessage());
        }
    }

    public void removeFinanceEntity(String entity) {
        try {
            String contractAddress = loadFinanceAddr();
            Finance_platform finance_platform = Finance_platform.load(contractAddress, client, cryptoKeyPair);
            TransactionReceipt receipt = finance_platform.removeEntity(entity);
            List<Finance_platform.RemoveEntityEventEventResponse> response = finance_platform.getRemoveEntityEventEvents(receipt);
            if (!response.isEmpty()) {
                if (response.get(0).ret == true) {
                    System.out.printf(
                            " removeFinanceEntity success => id: %s\n", entity);
                } else {
                    System.out.printf(
                            " removeFinanceEntity, ret code is %s \n", response.get(0).ret.toString());
                }
            } else {
                System.out.println(" event log not found, maybe transaction not exec. ");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();

            logger.error(" removeFinanceEntity exception, error message is {}", e.getMessage());
            System.out.printf(" removeFinanceEntity failed, error message is %s\n", e.getMessage());
        }
    }

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println(
                "\t java -cp conf/:lib/*:apps/* org.fisco.bcos.finance.client.finance_platform_client deploy");
        System.out.println(
                "\t java -cp conf/:lib/*:apps/* org.fisco.bcos.finance.client.finance_platform_client queryEntity account");
        System.out.println(
                "\t java -cp conf/:lib/*:apps/* org.fisco.bcos.finance.client.finance_platform_client queryTransaction id");
        System.out.println(
                "\t java -cp conf/:lib/*:apps/* org.fisco.bcos.finance.client.finance_platform_client register account value");
        System.out.println(
                "\t java -cp conf/:lib/*:apps/* org.fisco.bcos.finance.client.finance_platform_client transfer from_account to_account amount");
        System.out.println(
                "\t java -cp conf/:lib/*:apps/* org.fisco.bcos.finance.client.finance_platform_client transactionUpload id acc1 acc2 money");
        System.out.println(
                "\t java -cp conf/:lib/*:apps/* org.fisco.bcos.finance.client.finance_platform_client transactionUpdate id money");
        System.out.println(
                "\t java -cp conf/:lib/*:apps/* org.fisco.bcos.finance.client.finance_platform_client splitFinanceTransaction  old_id new_id new_acc money");
        System.out.println(
                "\t java -cp conf/:lib/*:apps/* org.fisco.bcos.finance.client.finance_platform_client removeFinanceTransaction id");
        System.out.println(
                "\t java -cp conf/:lib/*:apps/* org.fisco.bcos.finance.client.finance_platform_client removeFinanceEntity acc");
        System.exit(0);
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            Usage();
        }

        finance_platform_client client = new finance_platform_client();
        client.initialize();

        switch (args[0]) {
            case "deploy":
                client.deployFinanceAndRecordAddr();
                break;
            case "queryEntity":
                if (args.length < 2) {
                    Usage();
                }
                client.queryFinanceAmount(args[1]);
                break;
            case "queryTransaction":
                if (args.length < 2) {
                    Usage();
                }
                client.queryTransaction(args[1]);
                break;
            case "register":
                if (args.length < 3) {
                    Usage();
                }
                client.registerFinanceAccount(args[1], new BigInteger(args[2]));
                break;
            case "transfer":
                if (args.length < 4) {
                    Usage();
                }
                client.transferFinance(args[1], args[2], new BigInteger(args[3]));
                break;
            case "transactionUpload":
                if (args.length < 5) {
                    Usage();
                }
                client.transactionUpload(args[1], args[2], args[3], new BigInteger(args[4]));
                break;
            case "splitFinanceTransaction":
                if (args.length < 5) {
                    Usage();
                }
                client.splitFinanceTransaction(args[1], args[2], args[3], new BigInteger(args[4]));
                break;
            case "removeFinanceTransaction":
                if (args.length < 2) {
                    Usage();
                }
                client.removeFinanceTransaction(args[1]);
                break;
            case "removeFinanceEntity":
                if (args.length < 2) {
                    Usage();
                }
                client.removeFinanceEntity(args[1]);
                break;
            case "transactionUpdate":
                if (args.length < 3) {
                    Usage();
                }
                client.transactionUpdate(args[1],new BigInteger(args[2]));
                break;
            default:
            {
                Usage();
            }
        }
        System.exit(0);
    }

}