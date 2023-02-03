package com.mercado_solidario.api.core.security;

import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import com.mercado_solidario.api.core.JpaUserDetailsService;

@SuppressWarnings("deprecation")
@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter{
	
	@Autowired
	private JpaUserDetailsService jpaUserDetailsService;
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth)throws Exception {
		auth.inMemoryAuthentication()
			.withUser("Test")
				.password( passwordEncoder().encode("123"))
				.roles("ADMIN");
		
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
	/*	http.httpBasic()
			.and()
			.authorizeHttpRequests()
				.anyRequest().authenticated()
			.and()
				.sessionManagement()
					.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and()	
				.csrf().disable();//desabilita cookies para proteger contra ataques*/
		//super.configure(http);
		http.csrf().disable()
		/*authorizeRequests()//.anyRequest().permitAll()
				.antMatchers(HttpMethod.GET).permitAll()
				.antMatchers(HttpMethod.POST, "/usuarios").permitAll()
				.antMatchers(HttpMethod.POST).hasAuthority("POST_PATCH_ALLOWED")
				.antMatchers(HttpMethod.PATCH).hasAuthority("POST_PATCH_ALLOWED")
				.antMatchers(HttpMethod.PATCH, "/usuarios").hasAuthority("ADMIN_USER")
				.antMatchers(HttpMethod.DELETE).hasAuthority("DELETE_ALLOWED")
				.antMatchers(HttpMethod.POST, "/pedidos").hasAuthority("CREATE_PEDIDO")
				.anyRequest().authenticated()*/
			
			//.and()
			.cors().and()
			.oauth2ResourceServer()
				.jwt()
				.jwtAuthenticationConverter(jwtAuthenticationConverterInstaciator());
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	private JwtAuthenticationConverter jwtAuthenticationConverterInstaciator() {
		var jwtAuthenticationConverterInstance = new JwtAuthenticationConverter();
		jwtAuthenticationConverterInstance.setJwtGrantedAuthoritiesConverter(jwt -> {
			var authorities = jwt.getClaimAsStringList("autorities");
			if( authorities == null)authorities = Collections.emptyList();
			
			return authorities.stream()
								.map(SimpleGrantedAuthority::new)
								.collect(Collectors.toList());
		});
		
		return jwtAuthenticationConverterInstance;
	}
	
	@Bean
	@Override
	protected AuthenticationManager authenticationManager() throws Exception{
		return super.authenticationManager();
	}
	
	/*@Bean
	@Override
	protected UserDetailsService userDetailsService() {
		return super.userDetailsService();
	}*/

}
