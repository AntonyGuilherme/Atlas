package application;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import config.Config;
import infrastructure.repositories.WordRockRepository;
import org.rocksdb.RocksIterator;

import java.nio.charset.StandardCharsets;

public class RangesAnnouncer implements Runnable {
    final Queues queues;
    final Integer agentId;

    public RangesAnnouncer(Queues queues, Integer agentId) {
        this.queues = queues;
        this.agentId = agentId;
    }

    @Override
    public void run() {
        RangeMap<Integer, Integer> ranges = getRangeMap();

        WordRockRepository repository = new WordRockRepository();
        repository.init();

        try (RocksIterator iterator = repository.getIterator()) {
            for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                long freq = WordRockRepository.ByteUtils.bytesToLong(iterator.value());
                Integer agentId = ranges.get((int) freq);

                if (this.agentId.equals(agentId)) continue;

                String word = new String(iterator.key(), StandardCharsets.UTF_8);
                queues.wordsByAgent.get(agentId).add(word);
            }
        }
    }

    private RangeMap<Integer, Integer> getRangeMap() {
        int max = 0;
        int min = Integer.MAX_VALUE;

        for (String range : this.queues.ranges){
            String[] ranges = range.split(",");
            int partialMin = Integer.parseInt(ranges[0]);
            int partialMax = Integer.parseInt(ranges[1]);

            if (partialMax > max) max = partialMin;
            if (partialMin < min) min = partialMax;
        }

        int difference = (int) (0.5 + (double) (max - min) / Config.NUMBER_OF_AGENTS);

        RangeMap<Integer, Integer> ranges = TreeRangeMap.create();

        for (int i = 0; i < Config.NUMBER_OF_AGENTS; i++) {
            int agentMin = max - difference * (i + 1);
            int agentMax = agentMin + difference;
            if (agentMin > 1) agentMin++;
            ranges.put(Range.closed(agentMin, agentMax), i);
        }
        return ranges;
    }
}
