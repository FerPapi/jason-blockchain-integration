// package example_blockchain.src.env;
package env;


import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.ObsProperty;

// import needed libraries for java and web3j
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.parity.Parity;
import org.web3j.protocol.parity.methods.response.PersonalUnlockAccount;
import org.web3j.tx.ClientTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.utils.Convert;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.Utf8String;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Future;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.tx.Contract;

import env.Counter;

public class GUIConsole extends Artifact {

    private Display display;
    private Web3j web3 = Web3j.build(new HttpService());  // defaults to http://localhost:8545/
    private Parity parity = Parity.build(new HttpService()); // defaults to http://localhost:8545/

    void init(String name) {
        // creates an observable property called numMsg
        this.defineObsProperty("Count", 0);
        display = new Display(name);
        display.setVisible(true);

    }

    // implements an operation available to the agents
    @OPERATION void printMsg(String msg){
        String agentName = this.getOpUserName();
        ObsProperty prop = this.getObsProperty("numMsg");
        prop.updateValue(prop.intValue()+1);
        display.addText("Message at "+System.currentTimeMillis()+" from "+agentName+": "+msg);
        display.updateNumMsgField(prop.intValue());
    }

    // @OPERATION void deployContract(){
    //     try{
    //         Web3j web3 = Web3j.build(new HttpService());
    //         Counter contract = Counter.deploy(web3, )
    //
    //     } catch(Exception e) {
    //         e.printStackTrace();
    //     }
    // }


    @OPERATION void getNode(String msg){
        try{
          String agentName = this.getOpUserName();
          Web3ClientVersion web3ClientVersion = web3.web3ClientVersion().send();
          String clientVersion = web3ClientVersion.getWeb3ClientVersion();

          File walletfile = new File("/home/fernandopapi/.ethereum/testnet/keystore/UTC--2017-10-16T10-52-22.825000000Z--54d7e3720bad7742591498b0f2872613c32d7812");
          Credentials credentials = WalletUtils.loadCredentials("walletfilepassword", walletfile);
          BigInteger gasPrice = BigInteger.valueOf(20_000_000_000L);
          BigInteger gasLimit = BigInteger.valueOf(4_300_000);
          Counter counterContract = Counter.load("0x683a7c7F2eB69e8Dc6061B5712e2F7BBAdB38893", web3, credentials, gasPrice, gasLimit);
          BigInteger result = counterContract.getCount().get().getValue();

          display.addText("Client Version: clientVersion" + "\n");
          display.addText("Wallet Address: 0x54d7e3720bad7742591498b0f2872613c32d7812" + "\n");
          display.addText("Wallet Filepath: " + walletfile + "\n");
          display.addText("Blockchain Count Function: " + result + "\n");
          display.addText("Incrementing: " + "\n");

          TransactionReceipt transactionReceipt = counterContract.increment().get();

          result = counterContract.getCount().get().getValue();
          display.addText("Blockchain Count Function: " + result + "\n");


        } catch (Exception e) {
          e.printStackTrace();
        }
    }

    static class Display extends JFrame {

        private JTextArea text;
        private JLabel numMsg;
        private static int n = 0;

        public Display(String name) {
            setTitle(".:: "+name+" console ::.");

            JPanel panel = new JPanel();
            setContentPane(panel);
            panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));

            numMsg = new JLabel("0");
            text = new JTextArea(15,60);

            panel.add(text);
            panel.add(Box.createVerticalStrut(5));
            panel.add(numMsg);
            pack();
            setLocation(n*40, n*80);
            setVisible(true);

            n++;
        }

        public void addText(final String s){
            SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                    text.append(s+"\n");
                }
            });
        }

        public void updateNumMsgField(final int value){
            SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                    numMsg.setText(""+value);
                }
            });
        }
    }
}
