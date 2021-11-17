public final class EntityFactory {

    public OutConsumer createOutConsumer(Consumer consumer) {
        OutConsumer outConsumer = new OutConsumer();
        outConsumer.setisBankrupt(consumer.isBankrupt());
        outConsumer.setBudget(consumer.getInitialBudget());
        outConsumer.setId(consumer.getId());
        return outConsumer;
    }

    public OutDistributor createOutDistributor(Distributor distributor) {
        OutDistributor outDistributor = new OutDistributor();
        outDistributor.setisBankrupt(distributor.isBankrupt());
        outDistributor.setBudget(distributor.getInitialBudget());
        outDistributor.setId(distributor.getId());
        outDistributor.setContracts(distributor.getContracts());
        outDistributor.setEnergyNeededKW(distributor.getEnergyNeededKW());
        outDistributor.setContractCost((int) distributor.getContractPrice());
        outDistributor.setProducerStrategy(distributor.getProducerStrategy());
        return outDistributor;
    }

    public OutProducer createOutProducer(Producer producer) {
        OutProducer outProducer = new OutProducer();
        outProducer.setId(producer.getId());
        outProducer.setEnergyPerDistributor(producer.getEnergyPerDistributor());
        outProducer.setEnergyType(producer.getEnergyType());
        outProducer.setMaxDistributors(producer.getMaxDistributors());
        outProducer.setPriceKW(producer.getPriceKW());
        outProducer.setMonthlyStats(producer.getMonthlyStats());
        return outProducer;
    }

}
