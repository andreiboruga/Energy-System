import java.util.ArrayList;
import java.util.List;

public final class Output {

    private List<OutConsumer> consumers = new ArrayList<>();
    private List<OutDistributor> distributors = new ArrayList<>();
    private List<OutProducer> energyProducers = new ArrayList<>();

    public List<OutConsumer> getConsumers() {
        return consumers;
    }

    public void setConsumers(List<OutConsumer> consumers) {
        this.consumers = consumers;
    }

    public List<OutDistributor> getDistributors() {
        return distributors;
    }

    public void setDistributors(List<OutDistributor> distributors) {
        this.distributors = distributors;
    }

    public List<OutProducer> getEnergyProducers() {
        return energyProducers;
    }

    public void setEnergyProducers(List<OutProducer> energyProducers) {
        this.energyProducers = energyProducers;
    }
}
