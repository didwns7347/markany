
package com.markany.blinkist.controller;

import java.io.IOException;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.markany.blinkist.service.KakaoService;
import com.markany.blinkist.service.NaverLoginService;
import com.markany.blinkist.service.UserService;
import com.markany.blinkist.vo.UserVo;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private NaverLoginService naverLoginService;

	@Autowired
	UserService userService;


	//카카오톡 로그인 연동
	@RequestMapping("/kakaoLogin")
	public String home(@RequestParam(value = "code", required = false) String code,HttpServletRequest request,Model model) throws Exception {

		KakaoService service = new KakaoService();
		String access_Token = service.getAccessToken(code);
		HashMap<String, Object> userInfo = service.getUserInfo(access_Token);

		//사용자가 입력한 email가져오기
		String email = (String)userInfo.get("email");

		//user table에 헤당 이메일이 있는지 확인
		UserVo user =  userService.findByEmail(email);

		// 이메일이 있다면 로그인후 볼수 있는 view 보여주기
		if(user!=null) {

			//session저장
			HttpSession session = request.getSession();
			session.setAttribute("authUser", email);

			return "redirect:/";

		}else {	// 이메일이 없다면 회원가입시키기

			model.addAttribute("email",email);

			return "user/join";

		}
	}


	//네이버로그인연동페이지
	@RequestMapping("/login")
	public String login(HttpSession session, Model model) {
		String url=naverLoginService.getAuthorizationUrl(session);
		model.addAttribute("url", url);
		return "user/login";
	}


	//네이버연동처리
	@RequestMapping(value = "/callback", method = { RequestMethod.GET, RequestMethod.POST })
	public String callback(Model model, @RequestParam String code, @RequestParam String state, HttpSession session) throws IOException, ParseException {

		OAuth2AccessToken oauthToken = naverLoginService.getAccessToken(session, code, state);

		//로그인 사용자 정보를 읽어온다.
		String apiResult = naverLoginService.getUserProfile(oauthToken);

		//사용자가 입력한 email가져오기
		String email=naverLoginService.getUserEmail(apiResult);

		//user table에 헤당 이메일이 있는지 확인
		UserVo uservo = userService.findByEmail(email);

		// 이메일이 있다면 로그인후 볼수 있는 view 보여주기
		if(uservo!=null) {

			//session저장
			session.setAttribute("authUser", email);
			return "redirect:/";

		}
		else {	// 이메일이 없다면 회원가입시키기
			model.addAttribute("email",email);


			return "user/join";

		}

	}
	
	
	//로그아웃
	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	public String logout(HttpSession session) {
		
		session.removeAttribute("authUser");
		
		return "redirect:/";
		
		
	}


	//회원가입
	@RequestMapping("/join")
	public String join(UserVo uservo,HttpSession session) {
		
		//회원가입
		userService.insert(uservo);
		
		//세션에 회원의 이메일저장
		session.setAttribute("authUser", uservo.getEmail());
		
		return "redirect:/";
	}



	//회원정보수정GET
	@RequestMapping(value = "/update", method = RequestMethod.GET)
	public String getUpdate(HttpSession session,Model model) {

		//세션에 저장된 회원의 이메일정보가져오기
		String email = (String)session.getAttribute("authUser");

		//이메일을 토대로 회원의 정보가져오기
		UserVo uservo = userService.selectbyUser(email);

		model.addAttribute("uservo", uservo);

		return "user/update";

	}


	//회원정보수정POST
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public String postUpdate(HttpSession session,Model model,HttpServletRequest request) {


		//회원의 이메일 가져오기
		String email = (String)session.getAttribute("authUser");

		//기존의 비밀번호가져오기
		String oldpassword = (String)request.getParameter("oldpassword");

		//새로운 비밀번호가져오기
		String newpassword = (String)request.getParameter("newpassword");

		boolean result = userService.updatePw(email, oldpassword, newpassword);

		if(result) {

			model.addAttribute("message", "비밀번호를 변경하였습니다.");
			return "redirect:/";

		}else {

			model.addAttribute("message", "기존비밀번호를 잘못입력하셨습니다.확인해주세요.");
			return "redirect:/";

		}		
	}
	
	
	//회원탈퇴
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public String postDelete(HttpSession session,Model model) {

		//회원의 이메일 가져오기
		String email = (String)session.getAttribute("authUser");

        userService.deleteUser(email);
        
        session.removeAttribute("authUser");
			
        model.addAttribute("message", "회원탈퇴하였습니다.");
        
	    return "redirect:/";
	}

	
	@RequestMapping("/logout")
	public String join(HttpSession session) {
		session.removeAttribute("authUser");
		return "redirect:/";

	}
}