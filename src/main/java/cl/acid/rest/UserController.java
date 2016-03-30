package cl.acid.rest;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import cl.acid.rest.exception.BadRequestException;
import cl.acid.rest.exception.ErrorMessage;
import cl.acid.rest.exception.UnauthorizedUserException;
import cl.acid.rest.model.User;
import cl.acid.rest.model.UserRepository;

@RestController
@RequestMapping("${service.url}")
public class UserController {
	
	@Value("${image.maxSize}")
	private Integer imageMaxSize;
	
	@Value("${user.authorized}")
	private String authorizedUser;

	private final UserRepository userRepository;
	
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> add(@RequestBody User user) throws UnauthorizedUserException, BadRequestException   {
		
		validate(user);

		HttpHeaders httpHeaders = new HttpHeaders();
		User currentUser = userRepository.findByUsername(user.getUsername());
		if (currentUser == null){
			User newUser = userRepository.save(new User(user.getUsername(), user.getImage()));
			httpHeaders.setLocation(ServletUriComponentsBuilder
					.fromCurrentRequest().path("/{id}")
					.buildAndExpand(newUser.getId()).toUri());
		} else {
			currentUser.setImage(user.getImage());
			userRepository.save(currentUser);
			httpHeaders.setLocation(ServletUriComponentsBuilder
					.fromCurrentRequest().path("/{id}")
					.buildAndExpand(currentUser.getId()).toUri());
		}
		return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);	
	}
	
	@RequestMapping(value = "/{userId}", method = RequestMethod.GET)
	public ModelAndView readUser(@PathVariable String userId, HttpServletResponse response, Model model) {
		User user = this.userRepository.findOne(Integer.parseInt(userId));
	    if (user != null) {
			model.addAttribute("username", user.getUsername());
			model.addAttribute("imageUri", ServletUriComponentsBuilder
					.fromCurrentRequest().path("/image").build().toUri().toString());	
		}	   
	    return new ModelAndView("userImage");
	}
	
	@RequestMapping(value = "/{userId}/image", method = RequestMethod.GET)
	public void readImage(@PathVariable String userId, HttpServletResponse response) {
		User user = this.userRepository.findOne(Integer.parseInt(userId));
	    ServletOutputStream outputStream;
		try {
			outputStream = response.getOutputStream();
		    outputStream.write(user.getImage());
		    outputStream.flush();
		    outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	    
	}

	private void validate(User user) throws UnauthorizedUserException, BadRequestException {
		
		if (user.getUsername().equals("") || user.getImage().length == 0)
			throw new BadRequestException();
		
		if (!user.getUsername().equals(authorizedUser.trim()))
			throw new UnauthorizedUserException();
		
		if (user.getImage().length > imageMaxSize)
			throw new BadRequestException();		
		
		BufferedImage image;
		try {
			image = ImageIO.read(new ByteArrayInputStream(user.getImage()));
			if (image == null){
				throw new BadRequestException();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	@ExceptionHandler(UnauthorizedUserException.class)
	public ResponseEntity<ErrorMessage> handleUnauthorizedUser(HttpServletRequest req, Exception e) 
	{
		ErrorMessage errorMessage = new ErrorMessage("Unauthorized");
		return new ResponseEntity<ErrorMessage>(errorMessage, HttpStatus.UNAUTHORIZED);
	}	
	
	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ErrorMessage> handleBadRequest(HttpServletRequest req, Exception e) 
	{
		ErrorMessage errorMessage = new ErrorMessage("Bad Request");
		return new ResponseEntity<ErrorMessage>(errorMessage, HttpStatus.BAD_REQUEST);
	}	
    
    @Autowired
    UserController(UserRepository userRepository){
    	this.userRepository = userRepository;
    }
}
