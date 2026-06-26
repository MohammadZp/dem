package ir.dotin.exam.test.sample;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserService service;

    @Test
    void getUser() throws Exception {

        when(service.getUser(2L))

                .thenReturn(new User(3L, "Mohammad"));

        mockMvc.perform(

                        MockMvcRequestBuilders.get("/users/2")

                )

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(3))

                .andExpect(jsonPath("$.name").value("Mohammad"));

        verify(service).getUser(2L);
    }

    @Test
    void getUserNull() throws Exception {

        when(service.getUser(2L))

                .thenReturn(new User(3L, "Mohammad"));

        MvcResult result = mockMvc.perform(

                        MockMvcRequestBuilders.get("/users/2")

                )

                .andExpect(status().isOk()).andReturn();

        String json =
                result.getResponse()
                        .getContentAsString();

        ObjectMapper mapper =
                new ObjectMapper();

        User user =
                mapper.readValue(
                        json,
                        User.class
                );

        Assertions.assertEquals("Mohammad", user.getName());
    }

    @Test
    void getUseThrowException() throws Exception {

        when(service.getUser(2L))

                .thenThrow(UserNotFoundException.class);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/users/2")
                )
                .andExpect(status().isNotFound());

    }
}
