package com.testwithspring.master.web

import com.github.springtestdbunit.DbUnitTestExecutionListener
import com.github.springtestdbunit.annotation.DatabaseSetup
import com.github.springtestdbunit.annotation.DbUnitConfiguration
import com.github.springtestdbunit.annotation.ExpectedDatabase
import com.github.springtestdbunit.assertion.DatabaseAssertionMode
import com.github.springtestdbunit.dataset.ReplacementDataSetLoader
import com.testwithspring.master.IntegrationTest
import com.testwithspring.master.IntegrationTestContext
import com.testwithspring.master.Tasks
import com.testwithspring.master.Users
import com.testwithspring.master.config.Profiles
import org.junit.experimental.categories.Category
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import spock.lang.Specification

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@ContextConfiguration(classes = [IntegrationTestContext.class])
@WebAppConfiguration
@TestExecutionListeners(value = DbUnitTestExecutionListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
@DatabaseSetup([
        '/com/testwithspring/master/users.xml',
        '/com/testwithspring/master/tasks.xml'
])
@DbUnitConfiguration(dataSetLoader = ReplacementDataSetLoader.class)
@Category(IntegrationTest.class)
@ActiveProfiles(Profiles.INTEGRATION_TEST)
class ProcessValidUpdateTaskFormForUnknownTaskSpec extends Specification {

    private static final NEW_DESCRIPTION = 'The old lesson was not good'
    private static final NEW_TITLE = 'Rewrite an existing lesson'

    @Autowired
    WebApplicationContext webAppContext

    def mockMvc

    def setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .apply(springSecurity())
                .build()
    }

    @ExpectedDatabase(value = '/com/testwithspring/master/tasks.xml', assertionMode = DatabaseAssertionMode.NON_STRICT)
    def 'Submit a valid update task form as an anonymous user when the updated task is not found'() {

        def response

        when: 'An anonymous user submits a valid update task form'
        response = submitUpdateTaskForm()

        then: 'Should return the HTTP status code found'
        response.andExpect(status().isFound())

        and: 'Should redirect the user to the login page'
        response.andExpect(redirectedUrl(WebTestConstants.LOGIN_PAGE_URL))
    }

    @WithUserDetails(Users.JohnDoe.EMAIL_ADDRESS)
    @ExpectedDatabase(value = '/com/testwithspring/master/tasks.xml', assertionMode = DatabaseAssertionMode.NON_STRICT)
    def 'Submit a valid update task form as a registered user when the updated task is not found'() {

        def response

        when: 'A registered user submits a valid update task form'
        response = submitUpdateTaskForm()

        then: 'Should return the HTTP status code not found'
        response.andExpect(status().isNotFound())

        and: 'Should render the not found view'
        response.andExpect(view().name(WebTestConstants.ErrorView.NOT_FOUND))

        and: 'Should forward the user to the not found page'
        response.andExpect(forwardedUrl('/WEB-INF/jsp/error/404.jsp'))
    }

    @WithUserDetails(Users.AnneAdmin.EMAIL_ADDRESS)
    @ExpectedDatabase(value = '/com/testwithspring/master/tasks.xml', assertionMode = DatabaseAssertionMode.NON_STRICT)
    def 'Submit a valid update task form as an administrator when the updated task is not found'() {

        def response

        when: 'An administrator submits a valid update task form'
        response = submitUpdateTaskForm()

        then: 'Should return the HTTP status code not found'
        response.andExpect(status().isNotFound())

        and: 'Should render the not found view'
        response.andExpect(view().name(WebTestConstants.ErrorView.NOT_FOUND))

        and: 'Should forward the user to the not found page'
        response.andExpect(forwardedUrl('/WEB-INF/jsp/error/404.jsp'))
    }

    private ResultActions submitUpdateTaskForm() throws Exception {
        return  mockMvc.perform(post('/task/{taskId}/update', Tasks.TASK_ID_NOT_FOUND)
                .param(WebTestConstants.ModelAttributeProperty.Task.DESCRIPTION, NEW_DESCRIPTION)
                .param(WebTestConstants.ModelAttributeProperty.Task.ID, Tasks.TASK_ID_NOT_FOUND.toString())
                .param(WebTestConstants.ModelAttributeProperty.Task.TITLE, NEW_TITLE)
                .with(csrf())
        )
    }
}
