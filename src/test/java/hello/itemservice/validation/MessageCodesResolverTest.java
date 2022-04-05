package hello.itemservice.validation;

import org.junit.jupiter.api.Test;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;
import static org.assertj.core.api.Assertions.assertThat;

public class MessageCodesResolverTest {

    MessageCodesResolver codesResolver = new DefaultMessageCodesResolver();

    @Test
    void messageCodesResolverObject() {
        String[] messageCodes = codesResolver.resolveMessageCodes("required", "item");

        for (String messageCode : messageCodes) {
            System.out.println(messageCode);
        }
    }

    @Test
    void messageCodesResolverField() {
        String[] messageCodes = codesResolver.resolveMessageCodes("required", "item", "itemName", String.class);
        String[] messageCodes1 = codesResolver.resolveMessageCodes("max", "item", "price", Integer.class);

        for (String code : messageCodes) {
            System.out.println(code);
        }
        for (String s : messageCodes1) {
            System.out.println(s);
        }
        //bindingResult.rejectValue("itemName", "required) ==>
        // ==> new FieldError("item", "itemName", null, false, messageCodes!!!!!, null, null);
        assertThat(messageCodes).containsExactly(
                "required.item.itemName",
                "required.itemName",
                "required.java.lang.String",
                "required"
        );
    }
}














