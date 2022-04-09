package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/validation/v2/items")
@RequiredArgsConstructor //final이 붙거나 @NotNull 이 붙은 필드의 생성자를 자동 생성해주는 롬복 어노테이션
public class ValidationItemControllerV2 {

    //@RequiredArgsConstructor + 생성자 하나면 autowired 생략 가능
    private final ItemRepository itemRepository;
    private final ItemValidator itemValidator;

    //RequiredArgsConstructor가 이걸 대신 해주는 거... 참고로 이 경우에도 Autowired 생략은 가능! 생성자 하나니까
//    @Autowired
//    public ValidationItemControllerV2(ItemRepository itemRepository, ItemValidator itemValidator) {
//        this.itemRepository = itemRepository;
//        this.itemValidator = itemValidator;
//    }

    @InitBinder //컨트롤러 호출될 때마다
    public void init(WebDataBinder dataBinder) {
        dataBinder.addValidators(itemValidator);

    }

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v2/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v2/addForm";
    }

//    @PostMapping("/add")
    public String addItemV1(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        //BindingResult bindingResult 파라미터의 위치는 @ModelAttribute Item item 다음에 와야 한다.

        //검증 로직
        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.addError(new FieldError("item", "itemName", "상품 이름은 필수입니다."));
        }
        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000){
            bindingResult.addError(new FieldError("item", "price", "가격은 1,000 ~ 1,000,000까지 허용합니다."));

        }
        if (item.getQuantity() == null || item.getQuantity() > 9999) {
            bindingResult.addError(new FieldError("item", "quantity", "수량은 최대 9,999까지 허용합니다.."));
        }

        //특정 필드가 아닌 복합 룰 검증
        if(item.getPrice() != null && item.getQuantity() != null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.addError(new ObjectError("item", "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice));
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        log.info("errors : {}  ", bindingResult);
        if(bindingResult.hasErrors()){
//            model.addAttribute("errors", errors);
            //bindingresult는 자동으로 view로 넘어감
            return "validation/v2/addForm";
        }

        //성공 로직

       Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true); //여기 들어가는 attribute는 String.. 쿼리파라미터니까 그러겠지?
        return "redirect:/validation/v2/items/{itemId}";
    }

//    @PostMapping("/add")
    public String addItemV2(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        //BindingResult bindingResult 파라미터의 위치는 @ModelAttribute Item item 다음에 와야 한다.

        //검증 로직
        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(),false, null, null,"상품 이름은 필수입니다."));
        }
        /*
        파라미터 목록
        objectName : 오류가 발생한 객체 이름 //model attribute
        field : 오류 필드
        rejectedValue : 사용자가 입력한 값(거절된 값)
        bindingFailure : 타입 오류 같은 바인딩 실패인지, 검증 실패인지 구분 값 // 이 경우에는 타입 오류는 아니고 검증 실패 오류니까 false
        codes : 메시지 코드
        arguments : 메시지에서 사용하는 인자
        defaultMessage : 기본 오류 메시지
        */

        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000){
            bindingResult.addError(new FieldError("item", "price", item.getPrice(),false, null, null,"가격은 1,000 ~ 1,000,000까지 허용합니다."));
        }
        if (item.getQuantity() == null || item.getQuantity() > 9999) {
            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(),false, null, null,"수량은 최대 9,999까지 허용합니다."));
        }

        //특정 필드가 아닌 복합 룰 검증
        if(item.getPrice() != null && item.getQuantity() != null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.addError(new ObjectError("item", null, null,"가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice));

            }
        }

        //검증에 실패하면 다시 입력 폼으로
        log.info("errors : {}  ", bindingResult);
        if(bindingResult.hasErrors()){
//            model.addAttribute("errors", errors);
            //bindingresult는 자동으로 view로 넘어감
            return "validation/v2/addForm";
        }

        //성공 로직

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true); //여기 들어가는 attribute는 String.. 쿼리파라미터니까 그러겠지?
        return "redirect:/validation/v2/items/{itemId}";
    }

//    @PostMapping("/add")
    public String addItemV3(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        //BindingResult bindingResult 파라미터의 위치는 @ModelAttribute Item item 다음에 와야 한다.

        //검증 로직
        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(),false, new String[]{"required.item.itemName"}, null,null));
        }
        /*
        파라미터 목록
        objectName : 오류가 발생한 객체 이름 //model attribute
        field : 오류 필드
        rejectedValue : 사용자가 입력한 값(거절된 값)
        bindingFailure : 타입 오류 같은 바인딩 실패인지, 검증 실패인지 구분 값 // 이 경우에는 타입 오류는 아니고 검증 실패 오류니까 false
        codes : 메시지 코드
        arguments : 메시지에서 사용하는 인자
        defaultMessage : 기본 오류 메시지
        */

        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000){
            bindingResult.addError(new FieldError("item", "price", "rkrur",false, new String[]{"range.item.price"}, new Object[]{1000,1000000},"가격은"));
        }
        if (item.getQuantity() == null || item.getQuantity() > 9999) {
            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(),false, new String[]{"max.item.quantity"}, new Object[] {9999},"수량은"));
        }

        //특정 필드가 아닌 복합 룰 검증
        if(item.getPrice() != null && item.getQuantity() != null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.addError(new ObjectError("item", new String[]{"totalPriceMin"}, new Object[]{10000, resultPrice},null));

            }
        }

        //검증에 실패하면 다시 입력 폼으로
        log.info("errors : {}  ", bindingResult);
        if(bindingResult.hasErrors()){
//            model.addAttribute("errors", errors);
            //bindingresult는 자동으로 view로 넘어감
            return "validation/v2/addForm";
        }

        //성공 로직

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true); //여기 들어가는 attribute는 String.. 쿼리파라미터니까 그러겠지?
        return "redirect:/validation/v2/items/{itemId}";
    }

//    @PostMapping("/add")
    public String addItemV4(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        //BindingResult bindingResult 파라미터의 위치는 @ModelAttribute Item item 다음에 와야 한다.

//        ValidationUtils.rejectIfEmptyOrWhitespace(bindingResult, "itemName","required"); 아래랑 똑같은 기능 코드
        //검증 로직
        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.rejectValue("itemName", "required"); // 검증할 대상을 알고 있으니까 필드 이름만
        }

        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000){
            bindingResult.rejectValue("price", "range", new Object[]{1000, 1000000}, null);
        }
        if (item.getQuantity() == null || item.getQuantity() > 9999) {
            bindingResult.rejectValue("quantity", "max", new Object[]{9999}, null);
        }

        //특정 필드가 아닌 복합 룰 검증
        if(item.getPrice() != null && item.getQuantity() != null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
//                bindingResult.addError(new ObjectError("item", new String[]{"totalPriceMin"}, new Object[]{10000, resultPrice},null));
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice},null);
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        log.info("errors : {}  ", bindingResult);
        if(bindingResult.hasErrors()){
//            model.addAttribute("errors", errors);
            //bindingresult는 자동으로 view로 넘어감
            return "validation/v2/addForm";
        }

        //성공 로직

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true); //여기 들어가는 attribute는 String.. 쿼리파라미터니까 그러겠지?
        return "redirect:/validation/v2/items/{itemId}";
    }

//    @PostMapping("/add")
    public String addItemV5(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        if (itemValidator.supports(item.getClass())) {
            itemValidator.validate(item, bindingResult);
        }

        //검증에 실패하면 다시 입력 폼으로
        log.info("errors : {}  ", bindingResult);
        if(bindingResult.hasErrors()){
//            model.addAttribute("errors", errors);
            //bindingresult는 자동으로 view로 넘어감
            return "validation/v2/addForm";
        }

        //성공 로직

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true); //여기 들어가는 attribute는 String.. 쿼리파라미터니까 그러겠지?
        return "redirect:/validation/v2/items/{itemId}";
    }

    @PostMapping("/add")
    public String addItemV6(@Validated @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
                            //@Validated <- Item에 대해서 검증기가 자동으로 수행됨
        //검증에 실패하면 다시 입력 폼으로
        log.info("errors : {}  ", bindingResult);
        if(bindingResult.hasErrors()){
//            model.addAttribute("errors", errors);
            //bindingresult는 자동으로 view로 넘어감
            return "validation/v2/addForm";
        }

        //성공 로직

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true); //여기 들어가는 attribute는 String.. 쿼리파라미터니까 그러겠지?
        return "redirect:/validation/v2/items/{itemId}";
    }


    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/validation/v2/items/{itemId}";
    }

}

