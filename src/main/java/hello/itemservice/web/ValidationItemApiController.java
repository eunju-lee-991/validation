package hello.itemservice.web;

import hello.itemservice.web.validation.form.ItemSaveForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/validation/api/items")
public class ValidationItemApiController {

    @PostMapping("/add")
    public Object addItem(@RequestBody @Validated ItemSaveForm form, BindingResult bindingResult) {
        // price에 문자를 넣게 되면 메세지컨버터가 json 데이터를 ItemSaveForm 객체로 바꾸는 거 자체를 실패해서 컨트롤러 호출 안됨. 예외처리 해줘야함

        log.info("API 컨트롤러 호출");

        if (bindingResult.hasErrors()) {
            log.info("error : {}", bindingResult);
            return bindingResult.hasErrors();
        }

        log.info("success");
        return form;
    }
}
