package com.b1nd.dodamdodam.nightstudy.infrastructure.swagger

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class SwaggerRedirectController {

    @GetMapping("/swagger-ui", "/swagger-ui/")
    fun redirectToSwaggerUi(): String = "redirect:/swagger-ui/index.html"
}
