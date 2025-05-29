import java.util.Map;

public class LocalEmitter implements Emitter {
    final Map<Integer, DistributedProcessor> process;

    public LocalEmitter(Map<Integer, DistributedProcessor> numberOfProcess) {
        process = numberOfProcess;
    }

    @Override
    public void emit(String word) {
        process.get(hash(word)).handle(word);
    }

    public int hash(String word) {
        long unsignedHash = word.hashCode() & 0xFFFFFFFFL;
        return (int) (unsignedHash % process.size());
    }

    public int size() {
        return process.size();
    }
}
