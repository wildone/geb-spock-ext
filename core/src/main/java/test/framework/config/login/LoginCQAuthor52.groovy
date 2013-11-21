package test.framework.config.login

import geb.Page

class LoginCQAuthor52 extends Page {

    static url = 'libs/wcm/auth/content/login.html'

    static content = {
        usernameField { $("#usr input") }
        passwordField { $("#pwd input") }
        loginButton { $("#submit input") }
    }

    void login(String username, String password) {
        usernameField.value username
        passwordField.value password
        loginButton.click()
    }
}
