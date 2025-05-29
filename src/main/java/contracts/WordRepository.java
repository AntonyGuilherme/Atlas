package contracts;

import java.util.Map;
import java.util.Queue;

public interface WordRepository {
    void save(Queue<String> words);
    Map<String, Integer> getAll();
    void close();
}
