import com.fasterxml.jackson.databind.ObjectMapper;
import strategies.EnergyChoiceStrategyType;

import java.io.File;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws Exception {
        File fin = new File(args[0]);
        File fout = new File(args[1]);
        ObjectMapper objectMapper = new ObjectMapper();
        Input input = objectMapper.readValue(fin, Input.class);
        Singleton singleton = Singleton.getInstance();
        CDContractFactory factory = new CDContractFactory();
        Consumer c;
        Distributor bestDistributor;
        boolean existaDistribuitori = false;

        for (Distributor distributor : input.getInitialData().getDistributors()) {
            if (distributor.getProducerStrategy() == EnergyChoiceStrategyType.GREEN) {
                distributor.setStrategy(new GreenStrategy());
            } else if (distributor.getProducerStrategy() == EnergyChoiceStrategyType.PRICE) {
                distributor.setStrategy(new PriceStrategy());
            } else {
                distributor.setStrategy(new QuantityStrategy());
            }

            distributor.setInitialProductionCost(distributor.getStrategy()
                    .chooseProducers(input.getInitialData().getProducers(),
                            distributor.getEnergyNeededKW(), distributor));

            distributor.setContractPrice();
            distributor.setContracts(new ArrayList<>());
            distributor.setFlag(false);
        }

        bestDistributor = singleton.cheapestDistributor(input.getInitialData().getDistributors());

        for (Consumer consumer : input.getInitialData().getConsumers()) {
            consumer.setInitialBudget(consumer.getInitialBudget() + consumer.getMonthlyIncome());
            factory.createContract(bestDistributor, consumer.getId());
            consumer.setContractState(true);
        }

        for (Distributor distributor : input.getInitialData().getDistributors()) {
            distributor.setInitialBudget(
                    distributor.getInitialBudget() - distributor.getInitialInfrastructureCost()
                            - distributor.getInitialProductionCost()
                            * distributor.getClientsNumber());
            if (distributor.getContracts() != null) {
                for (Contract contract : distributor.getContracts()) {
                    c = input.getInitialData().getConsumers().get(contract.getConsumerId());
                    if (c.getInitialBudget() >= contract.getPrice()) {
                        c.setInitialBudget(c.getInitialBudget() - (int) contract.getPrice());
                        distributor.setInitialBudget(
                                distributor.getInitialBudget() + (int) contract.getPrice());
                    } else {
                        Debt debt = new Debt();
                        debt.setDistributor(distributor);
                        debt.setValue(Math.round(Math.floor(1.2 * contract.getPrice())));
                        c.setDebt(debt);
                    }
                }
            }
            if (distributor.getInitialBudget() < 0) {
                distributor.setBankrupt(true);
                if (distributor.getContracts() != null) {
                    for (Contract contract : distributor.getContracts()) {
                        c = input.getInitialData().getConsumers().get(contract.getConsumerId());
                        c.setContractState(false);
                        c.setDebt(null);
                    }
                    distributor.getContracts().clear();
                }
            } else {
                existaDistribuitori = true;
            }
        }

        for (int i = 1; i <= input.getNumberOfTurns() && existaDistribuitori; i++) {
            if (input.getMonthlyUpdates().get(i - 1).getNewConsumers() != null) {
                for (Consumer consumer : input.getMonthlyUpdates().get(i - 1).getNewConsumers()) {
                    input.getInitialData().getConsumers().add(consumer);
                }
            }

            if (input.getMonthlyUpdates().get(i - 1).getDistributorChanges() != null) {
                for (DistributorChange distributorChange : input.getMonthlyUpdates().get(i - 1)
                        .getDistributorChanges()) {
                    input.getInitialData().getDistributors().get(distributorChange.getId())
                            .setInitialInfrastructureCost(
                                    distributorChange.getInfrastructureCost());
                }
            }

            for (Distributor distributor : input.getInitialData().getDistributors()) {
                distributor.setContractPrice();
            }

            bestDistributor =
                    singleton.cheapestDistributor(input.getInitialData().getDistributors());

            for (Distributor distributor : input.getInitialData().getDistributors()) {
                if (distributor.getContracts() != null) {
                    for (int j = 0; j < distributor.getContracts().size(); j++) {
                        distributor.getContracts().get(j).setRemainedContractMonths(
                                distributor.getContracts().get(j).getRemainedContractMonths() - 1);
                        if (distributor.getContracts().get(j).getRemainedContractMonths() == 0) {
                            c = input.getInitialData().getConsumers()
                                    .get(distributor.getContracts().get(j).getConsumerId());
                            c.setContractState(false);
                            distributor.getContracts().remove(j);
                            distributor.setClientsNumber(distributor.getClientsNumber() - 1);
                            j--;
                        }
                    }
                }
            }

            for (Consumer consumer : input.getInitialData().getConsumers()) {
                if (consumer.isBankrupt()) {
                    continue;
                }
                consumer.setInitialBudget(
                        consumer.getInitialBudget() + consumer.getMonthlyIncome());
                if (!consumer.getContractState()) {
                    factory.createContract(bestDistributor, consumer.getId());
                    consumer.setContractState(true);
                }
            }

            for (Distributor distributor : input.getInitialData().getDistributors()) {
                if (distributor.isBankrupt()) {
                    continue;
                }
                distributor.setInitialBudget(distributor.getInitialBudget()
                        - distributor.getInitialInfrastructureCost()
                        - distributor.getInitialProductionCost() * distributor.getClientsNumber());
                if (distributor.getContracts() != null) {
                    for (int j = 0; j < distributor.getContracts().size(); j++) {
                        c = input.getInitialData().getConsumers()
                                .get(distributor.getContracts().get(j).getConsumerId());
                        if (c.getInitialBudget() >= distributor.getContracts().get(j).getPrice()) {
                            if (c.getDebt() == null || c.getDebt().getDistributor().isBankrupt()) {
                                c.setInitialBudget(c.getInitialBudget()
                                        - (int) distributor.getContracts().get(j).getPrice());
                                distributor.setInitialBudget(distributor.getInitialBudget()
                                        + (int) distributor.getContracts().get(j).getPrice());
                                if (c.getDebt() != null
                                        && c.getDebt().getDistributor().isBankrupt()) {
                                    c.setDebt(null);
                                }
                            } else {
                                if (c.getInitialBudget()
                                        >= distributor.getContracts().get(j).getPrice()
                                        + c.getDebt().getValue()) {
                                    c.setInitialBudget(c.getInitialBudget()
                                            - (int) distributor.getContracts().get(j).getPrice()
                                            - (int) c.getDebt().getValue());
                                    distributor.setInitialBudget(distributor.getInitialBudget()
                                            + (int) distributor.getContracts().get(j).getPrice());
                                    c.getDebt().getDistributor().setInitialBudget(
                                            c.getDebt().getDistributor().getInitialBudget()
                                                    + (int) c.getDebt().getValue());
                                } else {
                                    if (c.getDebt().getDistributor() == distributor) {
                                        c.setBankrupt(true);
                                        c.setContractState(false);
                                        distributor.getContracts().remove(j);
                                        distributor.setClientsNumber(
                                                distributor.getClientsNumber() - 1);
                                        j--;
                                    } else {
                                        if (c.getDebt().getValue() > c.getInitialBudget()) {
                                            c.setBankrupt(true);
                                            c.setContractState(false);
                                            distributor.getContracts().remove(j);
                                            distributor.setClientsNumber(
                                                    distributor.getClientsNumber() - 1);
                                            j--;
                                        } else {
                                            c.setInitialBudget(c.getInitialBudget()
                                                    - (int) c.getDebt().getValue());
                                            c.getDebt().getDistributor().setInitialBudget(
                                                    c.getDebt().getDistributor()
                                                            .getInitialBudget()
                                                            + (int) c.getDebt().getValue());
                                            Debt debt = new Debt();
                                            debt.setDistributor(distributor);
                                            debt.setValue(Math.round(Math.floor(1.2
                                                    * distributor.getContracts().get(j)
                                                            .getPrice())));
                                            c.setDebt(debt);
                                        }
                                    }
                                }
                            }
                        } else {
                            if (c.getDebt() == null || c.getDebt().getDistributor().isBankrupt()) {
                                Debt debt = new Debt();
                                debt.setDistributor(distributor);
                                debt.setValue(Math.round(Math.floor(
                                        1.2 * distributor.getContracts().get(j).getPrice())));
                                c.setDebt(debt);
                            } else {
                                if (c.getDebt().getDistributor() == distributor) {
                                    c.setBankrupt(true);
                                    c.setContractState(false);
                                    distributor.getContracts().remove(j);
                                    distributor
                                            .setClientsNumber(distributor.getClientsNumber() - 1);
                                    j--;
                                } else {
                                    if (c.getDebt().getValue() > c.getInitialBudget()) {
                                        c.setBankrupt(true);
                                        c.setContractState(false);
                                        distributor.getContracts().remove(j);
                                        distributor.setClientsNumber(
                                                distributor.getClientsNumber() - 1);
                                        j--;
                                    } else {
                                        c.setInitialBudget(c.getInitialBudget()
                                                - (int) c.getDebt().getValue());
                                        c.getDebt().getDistributor().setInitialBudget(
                                                c.getDebt().getDistributor().getInitialBudget()
                                                        + (int) c.getDebt().getValue());
                                        Debt debt = new Debt();
                                        debt.setDistributor(distributor);
                                        debt.setValue(Math.round(Math.floor(1.2
                                                * distributor.getContracts().get(j).getPrice())));
                                        c.setDebt(debt);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            for (ProducerChange producerChange : input.getMonthlyUpdates().get(i - 1)
                    .getProducerChanges()) {
                input.getInitialData().getProducers().get(producerChange.getId())
                        .setEnergyPerDistributor(producerChange.getEnergyPerDistributor());
                for (Distributor distributor : input.getInitialData().getProducers()
                        .get(producerChange.getId()).getDistributors()) {
                    distributor.setFlag(true);
                }
            }

            existaDistribuitori = false;
            for (Distributor distributor : input.getInitialData().getDistributors()) {
                if (distributor.getInitialBudget() < 0) {
                    distributor.setBankrupt(true);
                    if (distributor.getContracts() != null) {
                        for (Contract contract : distributor.getContracts()) {
                            c = input.getInitialData().getConsumers()
                                    .get(contract.getConsumerId());
                            c.setContractState(false);
                            if (c.getDebt().getDistributor().equals(distributor)) {
                                c.setDebt(null);
                            }
                        }
                        distributor.getContracts().clear();
                    }

                    for (Producer producer : input.getInitialData().getProducers()) {
                        producer.getDistributors().remove(distributor);
                    }

                } else {
                    existaDistribuitori = true;
                }
            }

            for (Distributor distributor : input.getInitialData().getDistributors()) {
                if (distributor.isBankrupt()) {
                    continue;
                }
                if (distributor.getFlag()) {

                    for (Producer producer : input.getInitialData().getProducers()) {
                        producer.getDistributors().remove(distributor);
                    }

                    distributor.setInitialProductionCost(distributor.getStrategy()
                            .chooseProducers(input.getInitialData().getProducers(),
                                    distributor.getEnergyNeededKW(), distributor));
                    distributor.setFlag(false);
                }
            }

            for (Producer producer : input.getInitialData().getProducers()) {
                Stat stat = new Stat();
                stat.setMonth(i);
                stat.setDistributorsIds(new ArrayList<>());
                for (int j = 0; j < producer.getDistributors().size(); j++) {
                    if (producer.getDistributors().get(j).isBankrupt()) {
                        producer.getDistributors().remove(j);
                        j--;
                        continue;
                    }
                    stat.getDistributorsIds().add(producer.getDistributors().get(j).getId());
                }
                producer.getMonthlyStats().add(stat);
            }

        }

        for (Distributor distributor : input.getInitialData().getDistributors()) {
            if (distributor.getContracts() != null) {
                for (int j = 0; j < distributor.getContracts().size(); j++) {
                    distributor.getContracts().get(j).setRemainedContractMonths(
                            distributor.getContracts().get(j).getRemainedContractMonths() - 1);
                }
            }
        }

        EntityFactory entityFactory = new EntityFactory();
        Output output = new Output();
        output.setConsumers(new ArrayList<>());
        output.setDistributors(new ArrayList<>());
        output.setEnergyProducers(new ArrayList<>());
        for (Consumer consumer : input.getInitialData().getConsumers()) {
            output.getConsumers().add(entityFactory.createOutConsumer(consumer));
        }

        for (Distributor distributor : input.getInitialData().getDistributors()) {
            output.getDistributors().add(entityFactory.createOutDistributor(distributor));
        }

        for (Producer producer : input.getInitialData().getProducers()) {
            output.getEnergyProducers().add(entityFactory.createOutProducer(producer));
        }

        for (OutProducer outProducer : output.getEnergyProducers()) {
            outProducer.sortMonthlyStats();
        }

        objectMapper.writeValue(fout, output);

    }
}

