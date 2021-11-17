import java.util.List;

public final class Singleton {

    private static Singleton singleton = null;

    private Singleton() {

    }

    public static Singleton getInstance() {
        if (singleton == null) {
            singleton = new Singleton();
        }
        return singleton;
    }

    public Distributor cheapestDistributor(List<Distributor> distributors) {
        Distributor d = null;
        long min = Long.MAX_VALUE;
        for (Distributor distributor : distributors) {
            if (distributor.isBankrupt()) {
                continue;
            }
            if (distributor.getContractPrice() < min) {
                d = distributor;
                min = distributor.getContractPrice();
            }
        }
        return d;
    }

}
