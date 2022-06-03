package it.unisa.casper.refactor;

import it.unisa.casper.refactor.splitting_algorithm.SplitClasses;
import it.unisa.casper.refactor.splitting_algorithm.game_theory.GameTheorySplitClasses;
import it.unisa.casper.storage.beans.*;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.logging.Logger;

import static org.junit.Assert.assertTrue;
public class GameTheorySplitClassesTest {


    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void splitTrue() {
        Collection<ClassBean> splittedClasses = new ArrayList<>();
        boolean errorOccured = false;
        try {
            splittedClasses = new GameTheorySplitClasses().split(smelly, 0);
        } catch (Exception e) {
            errorOccured = true;
            e.getMessage();
        }
        assertTrue(splittedClasses.size() == 1);
        assertTrue(!errorOccured);
    }

    @Test
    public void splitFalse() {
        Collection<ClassBean> splittedClasses = new ArrayList<ClassBean>();
        boolean errorOccured = false;
        try {
            splittedClasses = new SplitClasses().split(noSmelly, 0);
        } catch (Exception e) {
            errorOccured = true;
            e.getMessage();
        }
        Logger log = Logger.getLogger(getClass().getName());
        log.info("\n" + (splittedClasses.size() == 1));
        assertTrue(splittedClasses.size() == 1);
        log.info("\nError occurred:" + errorOccured);
        assertTrue(!errorOccured);
    }

}
