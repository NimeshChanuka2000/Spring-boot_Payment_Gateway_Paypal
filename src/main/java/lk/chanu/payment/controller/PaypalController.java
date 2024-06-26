package lk.chanu.payment.controller;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import lk.chanu.payment.service.PaypalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PaypalController {

    private final PaypalService paypalService;

    @GetMapping("/")
    public String home(){
        return "index";
    }

    @PostMapping("/payment/create")
    public RedirectView createPayment(){
        try {
            String cancelUrl = "http://localhost:8080/payment/cancel";
            String successUrl = "http://localhost:8080/payment/success";
            Payment payment = paypalService.createPayment(
                    10.0,
                    "USD",
                    "Paypal",
                    "sale",
                    "Payment Description",
                    cancelUrl,
                    successUrl
            );

            for (Links links : payment.getLinks()) {
                if (links.getRel().equals("approval_url")){
                    return new RedirectView(links.getHref());
                }
            }
        }catch (PayPalRESTException e){
            log.error("Error occurred while creating PayPal payment", e);
        }
        return new RedirectView("/payment/error");
    }

    @GetMapping("/payment/success")
    public String paymentSuccess(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId
    ){
        try {
            Payment payment = paypalService.executePayment(paymentId, payerId);
            if (payment.getState().equals("approved")){
                return "PaymentSuccess";
            }
        }catch (PayPalRESTException e){
            log.error("Error occurred while payment success", e);
        }
        return "PaymentSuccess";
    }

    @GetMapping("/payment/cancel")
    public String paymentCancel(){
        return "PaymentCancel";
    }

    @GetMapping("/payment/error")
    public String paymentError(){
        return "PaymentError";
    }
}
