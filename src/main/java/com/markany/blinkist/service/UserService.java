package com.markany.blinkist.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.markany.blinkist.vo.UserDetailsVo;
import com.markany.blinkist.vo.UserVo;
import com.markany.blinkist.dao.*;

@Service
public class UserService implements UserDetailsService {

	@Autowired
	private UserRepository userDAO;

	public UserVo findByEmail(String email) {// 이메일 중복확인
		UserVo vo = new UserVo();
		vo.setEmail(email);
		// TODO Auto-generated method stub
		return userDAO.findByEmail(email);

	}

	public boolean insert(UserVo uservo) {// 회원가입
		// 비밀번호 암호화
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		uservo.setPassword(passwordEncoder.encode(uservo.getPassword()));
		return userDAO.insert(uservo);

	}

	public UserVo selectbyUser(String email) {// 회원정보가져오기
		return userDAO.selectbyUser(email);

	}

	public boolean updatePw(String email, String oldpassword, String newpassoword) {// 비밀번호변경
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		UserVo userVo=this.findByEmail(email);
		if(passwordEncoder.matches(oldpassword, userVo.getPassword())) {
			userDAO.updatePw(userVo.getUser_no(), passwordEncoder.encode(newpassoword));
			return true;
		}
		return false;
		

	}

	public boolean deleteUser(String email) {// 회원탈퇴
		return userDAO.deleteUser(email);
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		// 최종적으로 리턴해야할 객체
		UserDetailsVo userDetails = new UserDetailsVo();

		// 사용자 정보 select
		UserVo vo = userDAO.findByEmail(email);

		// 사용자 정보가 없으면 null처리
		if (vo == null) {
			//System.out.println("널널 널널 널ㄴ런ㄹ널널널너런러너런러너런러널너런러널너런러널너런러 email=" + email);
			return null;
		}
		// System.out.println(vo.toString());
		// System.out.println(vo.toString());
		//시큐리티 로그인 로직 수행
		userDetails.setEmail(vo.getEmail());
		userDetails.setPassword(vo.getPassword());
		List<String> authorities = new ArrayList<String>();
		authorities.add(vo.getGrade().toString());
		userDetails.setAuthorities(authorities);
		// System.out.println("USERDETAILS====================="+userDetails.toString());
		return userDetails;

	}

	public boolean updategrade(UserVo uservo) {// 회원등급변경

		return userDAO.updategrade(uservo);

	}

	public boolean PassPrimium(UserVo uservo) {// primium날짜가 지났다면 회원의 등급과 premium_date날짜를 바꿔준다.

		return userDAO.PassPrimium(uservo);

	}

}