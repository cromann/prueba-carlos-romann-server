package cl.acid.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import cl.acid.rest.model.User;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class UserControllerTest {

	private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    private MockMvc mockMvc;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream().filter(
                hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().get();

        Assert.assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }

    @Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void createImage() throws Exception {
        String bookmarkJson = json(new User(
                "usuario1", Base64.getDecoder().decode("R0lGODlhEAAQAMQfAHbDR0twLrPdlpHQaZGYjPn8983muYvSWIPNU3G/RNTtxTZZGn6TbVKCLJPmV4nkTOz35ajld8LmqV16Rn7VR4DLT4zlUExgPHGLXIvOYKTad4DZSGm4PlJnQv///////yH5BAEAAB8ALAAAAAAQABAAAAWE4Cd+Xml6Y0pORMswE6p6mGFIUR41skgbh+ABgag4eL4OkFisDAwZxwLl6QiGzQHEM3AEqFZmJbNVICxfkjWjgAwUHkECYJmqBQNJYSuf18ECAABafQkJD3ZVGhyChoYcHH8+FwcJkJccD2kjHpQPFKAbmh4FMxcNqKhTPSknJiqwsSMhADs=")));
        this.mockMvc.perform(post("/users")
                .contentType(contentType)
                .content(bookmarkJson))
                .andExpect(status().isCreated());
    }
    
    @Test
    public void unauthorizedUser() throws Exception {
        String bookmarkJson = json(new User(
                "usuario2", Base64.getDecoder().decode("R0lGODlhEAAQAMQfAHbDR0twLrPdlpHQaZGYjPn8983muYvSWIPNU3G/RNTtxTZZGn6TbVKCLJPmV4nkTOz35ajld8LmqV16Rn7VR4DLT4zlUExgPHGLXIvOYKTad4DZSGm4PlJnQv///////yH5BAEAAB8ALAAAAAAQABAAAAWE4Cd+Xml6Y0pORMswE6p6mGFIUR41skgbh+ABgag4eL4OkFisDAwZxwLl6QiGzQHEM3AEqFZmJbNVICxfkjWjgAwUHkECYJmqBQNJYSuf18ECAABafQkJD3ZVGhyChoYcHH8+FwcJkJccD2kjHpQPFKAbmh4FMxcNqKhTPSknJiqwsSMhADs=")));
        this.mockMvc.perform(post("/users")
                .contentType(contentType)
                .content(bookmarkJson))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    public void badRequestEmptyUsername() throws Exception {
        String bookmarkJson = json(new User(
                "", Base64.getDecoder().decode("R0lGODlhEAAQAMQfAHbDR0twLrPdlpHQaZGYjPn8983muYvSWIPNU3G/RNTtxTZZGn6TbVKCLJPmV4nkTOz35ajld8LmqV16Rn7VR4DLT4zlUExgPHGLXIvOYKTad4DZSGm4PlJnQv///////yH5BAEAAB8ALAAAAAAQABAAAAWE4Cd+Xml6Y0pORMswE6p6mGFIUR41skgbh+ABgag4eL4OkFisDAwZxwLl6QiGzQHEM3AEqFZmJbNVICxfkjWjgAwUHkECYJmqBQNJYSuf18ECAABafQkJD3ZVGhyChoYcHH8+FwcJkJccD2kjHpQPFKAbmh4FMxcNqKhTPSknJiqwsSMhADs=")));
        this.mockMvc.perform(post("/users")
                .contentType(contentType)
                .content(bookmarkJson))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    public void badRequestEmptyImage() throws Exception {
        String bookmarkJson = json(new User(
                "usuario1", new byte[0]));
        this.mockMvc.perform(post("/users")
                .contentType(contentType)
                .content(bookmarkJson))
                .andExpect(status().isBadRequest());
    }

    protected String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
	
}
