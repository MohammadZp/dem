package ir.dotin.exam.di;

public class Main {
    public static void main(String[] args) {
        CardRepository cardRepository = new CardRepository();
        CardService cardService = new CardService(cardRepository);

        ApplicationContext applicationContext = new ApplicationContext();
        applicationContext.addToContext(CardRepository.class, new CardRepository());
        applicationContext.addToContext(CardService.class, cardService);


        Object object = applicationContext.getBean(CardService.class);
        Object object2 = applicationContext.getBean(CardService.class);

        System.out.println(object2 == object);
    }
}
