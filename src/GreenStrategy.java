import java.util.ArrayList;
import java.util.List;

public final class GreenStrategy implements Strategy {

    @Override
    public int chooseProducers(List<Producer> producers, int energyNeeded,
                               Distributor distributor) {

        int energy = 0;
        double productionCost = 0;
        List<Producer> sortedProducers = new ArrayList<>();

        sortedProducers.addAll(producers);
        for (int i = 0; i < sortedProducers.size() - 1; i++) {
            for (int j = i + 1; j < sortedProducers.size(); j++) {
                if (sortedProducers.get(i).getEnergyType().isRenewable()
                        == sortedProducers.get(j).getEnergyType().isRenewable()) {
                    if (sortedProducers.get(i).getPriceKW()
                            > sortedProducers.get(j).getPriceKW()) {
                        Producer producer = sortedProducers.get(i);
                        sortedProducers.set(i, sortedProducers.get(j));
                        sortedProducers.set(j, producer);
                    } else if (sortedProducers.get(i).getPriceKW()
                            == sortedProducers.get(j).getPriceKW()
                            && sortedProducers.get(i).getEnergyPerDistributor()
                            < sortedProducers.get(j).getEnergyPerDistributor()) {
                        Producer producer = sortedProducers.get(i);
                        sortedProducers.set(i, sortedProducers.get(j));
                        sortedProducers.set(j, producer);
                    }
                } else if (sortedProducers.get(j).getEnergyType().isRenewable()) {
                    Producer producer = sortedProducers.get(i);
                    sortedProducers.set(i, sortedProducers.get(j));
                    sortedProducers.set(j, producer);
                }
            }
        }

        for (Producer producer : sortedProducers) {
            if (energy >= energyNeeded) {
                break;
            }
            if (producer.getDistributors().size() == producer.getMaxDistributors()) {
                continue;
            }
            producer.getDistributors().add(distributor);
            energy += producer.getEnergyPerDistributor();
            productionCost =
                    productionCost + (producer.getEnergyPerDistributor() * producer.getPriceKW());
        }

        return (int) Math.round(Math.floor(productionCost / 10));

    }
}
