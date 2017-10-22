package mining;

import jason.asSyntax.Atom;
import jason.asSyntax.Term;
import jason.environment.grid.Location;

import java.util.logging.Logger;

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

public class MiningPlanet extends Artifact {

    private static Logger logger = Logger.getLogger(MiningPlanet.class.getName());

    static WorldModel  model = null;
    static WorldView   view;

    static int     simId    = 5; // type of environment
    static int     sleep    = 200;
    static boolean hasGUI   = true;

    int     agId     = -1;

    public enum Move {
        UP, DOWN, RIGHT, LEFT
    };

    @OPERATION
    public void init(int scenario, int agId) {
        this.agId = agId;
        initWorld(scenario);
    }

    public int getSimId() {
        return simId;
    }

    public void setSleep(int s) {
        sleep = s;
    }

    @OPERATION void up() throws Exception {     move(Move.UP);    }
    @OPERATION void down() throws Exception {   move(Move.DOWN);  }
    @OPERATION void right() throws Exception {  move(Move.RIGHT); }
    @OPERATION void left() throws Exception {   move(Move.LEFT);  }
    void move(Move m) throws Exception {
        if (sleep > 0) await_time(sleep);
    	model.move(m, agId);
        updateAgPercept();
    }

    @OPERATION void pick() throws Exception {
        if (sleep > 0) await_time(sleep);
    	model.pick(agId);
        updateAgPercept();
    }
    @OPERATION void drop() throws Exception {
        if (sleep > 0) await_time(sleep);
    	model.drop(agId);
        view.udpateCollectedGolds();
        updateAgPercept();
    }
    @OPERATION void skip() {
        if (sleep > 0) await_time(sleep);
        updateAgPercept();
    }

    public synchronized void initWorld(int w) {
        simId = w;
        try {
        	if (model == null) {
	            switch (w) {
	            case 1: model = WorldModel.world1(); break;
	            case 2: model = WorldModel.world2(); break;
	            case 3: model = WorldModel.world3(); break;
	            case 4: model = WorldModel.world4(); break;
	            case 5: model = WorldModel.world5(); break;
	            case 6: model = WorldModel.world6(); break;
	            default:
	                logger.info("Invalid index!");
	                return;
	            }
	            if (hasGUI) {
	                view = new WorldView(model);
	                view.setEnv(this);
	                view.udpateCollectedGolds();
	            }
	        }
            defineObsProperty("gsize", simId, model.getWidth(), model.getHeight());
            defineObsProperty("depot", simId, model.getDepot().x, model.getDepot().y);
            defineObsProperty("pos", -1, -1);
            updateAgPercept();
            //informAgsEnvironmentChanged();
        } catch (Exception e) {
            logger.warning("Error creating world "+e);
            e.printStackTrace();
        }
    }

    public void endSimulation() {
        defineObsProperty("end_of_simulation", simId, 0);
        //informAgsEnvironmentChanged();
        if (view != null) view.setVisible(false);
        WorldModel.destroy();
    }

    private void updateAgPercept() {
        // its location
        Location l = model.getAgPos(agId);
        ObsProperty p = getObsProperty("pos");
        p.updateValue(0, l.x);
        p.updateValue(1, l.y);

        if (model.isCarryingGold(agId)) {
        	if (!hasObsProperty("carrying_gold"))
        		defineObsProperty("carrying_gold");
        } else try {
        	removeObsProperty("carrying_gold");
        } catch (IllegalArgumentException e) {}

        // what's around
        updateAgPercept(l.x - 1, l.y - 1);
        updateAgPercept(l.x - 1, l.y);
        updateAgPercept(l.x - 1, l.y + 1);
        updateAgPercept(l.x, l.y - 1);
        updateAgPercept(l.x, l.y);
        updateAgPercept(l.x, l.y + 1);
        updateAgPercept(l.x + 1, l.y - 1);
        updateAgPercept(l.x + 1, l.y);
        updateAgPercept(l.x + 1, l.y + 1);

        //view.update();
    }

    private static Term gold     = new Atom("gold");
    private static Term obstacle = new Atom("obstacle");

    private void updateAgPercept(int x, int y) {
        if (model == null || !model.inGrid(x,y)) return;

        // remove all first
        try {
        	removeObsPropertyByTemplate("cell", null, null, null);
        } catch (IllegalArgumentException e) {}

        if (model.hasObject(WorldModel.OBSTACLE, x, y)) {
            defineObsProperty("cell", x, y, obstacle);
        } else if (model.hasObject(WorldModel.GOLD, x, y)) {
            defineObsProperty("cell", x, y, gold);
        }

        //if (model.hasObject(WorldModel.ENEMY, x, y))
        //    defineObsProperty("cell", x, y, "enemy");
        //if (model.hasObject(WorldModel.AGENT, x, y))
        //    defineObsProperty("cell", x, y, "ally");
    }

}
