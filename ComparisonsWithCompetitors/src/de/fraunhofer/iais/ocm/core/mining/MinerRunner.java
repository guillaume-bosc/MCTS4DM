package de.fraunhofer.iais.ocm.core.mining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;
import de.fraunhofer.iais.ocm.core.model.utility.PatternUtilityModel;

/**
 * User: paveltokmakov
 * Date: 5/17/13
 */
public class MinerRunner {

    private MiningAlgorithm miner;

    private MinerCallable currentTask;

    private final class MinerCallable implements Callable<Collection<Pattern>> {

        private DataTable dataTable;

        private PatternUtilityModel patternUtilityModel;

        private MinerCallable(DataTable dataTable, PatternUtilityModel patternUtilityModel) {
            this.dataTable = dataTable;
            this.patternUtilityModel = patternUtilityModel;
        }

        public Collection<Pattern> call() throws Exception {
            miner.mine(dataTable, patternUtilityModel);
            return miner.getResults();
        }

        public void stop() {
            miner.setStop(true);
        }
    }

    public MinerRunner(MiningAlgorithm miner) {
        this.miner = miner;
    }

    public Collection<Pattern> mine(DataTable dataTable, PatternUtilityModel patternUtilityModel) {
        return mine(dataTable, patternUtilityModel, -1);
    }

    public Collection<Pattern> mine(DataTable dataTable, PatternUtilityModel patternUtilityModel, int period)  {
        Collection<Pattern> results;

		currentTask = new MinerCallable(dataTable, patternUtilityModel);
        ExecutorService newSingleThreadExecutor = Executors.newSingleThreadExecutor();
		Future<Collection<Pattern>> fut = newSingleThreadExecutor.submit(currentTask);

        try {
            if(period == -1) {
                results = fut.get();
            } else {
                results = fut.get(period, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            currentTask.stop();
			e.printStackTrace();
		} catch (ExecutionException e) {
            currentTask.stop();
			e.printStackTrace();
		} catch (TimeoutException e) {
            currentTask.stop();
			try {
                newSingleThreadExecutor.shutdown();
				newSingleThreadExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			fut.cancel(true);
		} finally {
            results = miner.getResults();
        }
		newSingleThreadExecutor.shutdownNow();

        if(results == null) {
            results = new ArrayList<Pattern>();
        }

        return results;
    }

    public void stop() {
        if(currentTask == null) {
            return;
        }

        currentTask.stop();
    }

}
