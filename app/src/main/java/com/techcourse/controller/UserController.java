package com.techcourse.controller;

import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.techcourse.domain.User;
import com.techcourse.repository.InMemoryUserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nextstep.mvc.view.JsonView;
import nextstep.mvc.view.JspView;
import nextstep.mvc.view.ModelAndView;
import nextstep.web.annotation.Controller;
import nextstep.web.annotation.RequestMapping;
import nextstep.web.support.RequestMethod;

@Controller
public class UserController {

	private static final Logger log = LoggerFactory.getLogger(UserController.class);

	@RequestMapping(value = "/api/user", method = RequestMethod.GET)
	public ModelAndView findUser(HttpServletRequest request, HttpServletResponse response) {
		final String account = request.getParameter("account");
		log.debug("user id : {}", account);

		ModelAndView modelAndView;
		try {
			final User user = InMemoryUserRepository.findByAccount(account)
				.orElseThrow();
			modelAndView = new ModelAndView(new JsonView());
			modelAndView.addObject("user", user);
		} catch (NoSuchElementException e) {
			modelAndView = new ModelAndView(new JspView("404.jsp"));
		}
		return modelAndView;
	}
}
