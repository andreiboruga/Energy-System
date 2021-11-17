import java.util.List;

public final class Update {

    private List<Consumer> newConsumers;
    private List<DistributorChange> distributorChanges;
    private List<ProducerChange> producerChanges;

    public List<Consumer> getNewConsumers() {
        return newConsumers;
    }

    public void setNewConsumers(List<Consumer> newConsumers) {
        this.newConsumers = newConsumers;
    }

    public List<DistributorChange> getDistributorChanges() {
        return distributorChanges;
    }

    public void setDistributorChanges(List<DistributorChange> distributorChanges) {
        this.distributorChanges = distributorChanges;
    }

    public List<ProducerChange> getProducerChanges() {
        return producerChanges;
    }

    public void setProducerChanges(List<ProducerChange> producerChanges) {
        this.producerChanges = producerChanges;
    }
}
