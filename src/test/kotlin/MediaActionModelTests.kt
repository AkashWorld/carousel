import client.models.Action
import client.models.ClientContextImpl
import client.models.MediaAction
import client.models.MediaActionModelImpl
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.jupiter.api.*
import server.Server
import java.util.concurrent.CompletableFuture

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class MediaActionModelTests {
    companion object {
        @JvmStatic
        @BeforeAll
        fun startServer() {
            Server.getInstance().initialize()
            ClientContextImpl.getInstance()
                .requestSignInToken("test", "localhost", null, {}, {})
            Thread.sleep(1000)
        }
    }

    @Test
    fun modelSubscriptionPauseTest() {
        val firstSubResultFuture = CompletableFuture<MediaAction>()
        val mediaActionModel = MediaActionModelImpl()
        mediaActionModel.subscribeToActions { assert(false) }
        val obs = mediaActionModel.getMediaActionObservable()
        obs.addListener { _, oldValue, newValue ->
            assert(oldValue == null)
            if (newValue != null) {
                firstSubResultFuture.complete(newValue)
            } else {
                assert(false)
            }
        }
        GlobalScope.launch {
            delay(500)
            mediaActionModel.setPauseAction {
                assert(false)
            }
        }
        val action = firstSubResultFuture.get()
        assert(action.action == Action.PAUSE)
    }

    @Test
    fun modelSubscriptionPlayTest() {
        val firstSubResultFuture = CompletableFuture<MediaAction>()
        val mediaActionModel = MediaActionModelImpl()
        mediaActionModel.subscribeToActions { assert(false) }
        val obs = mediaActionModel.getMediaActionObservable()
        obs.addListener { _, oldValue, newValue ->
            assert(oldValue == null)
            if (newValue != null) {
                firstSubResultFuture.complete(newValue)
            } else {
                assert(false)
            }
        }
        GlobalScope.launch {
            delay(100)
            mediaActionModel.setPlayAction {
                assert(false)
            }
        }
        val action = firstSubResultFuture.get()
        assert(action.action == Action.PLAY)
    }

    @Test
    fun modelSubscriptionSeekTest() {
        val firstSubResultFuture = CompletableFuture<MediaAction>()
        val mediaActionModel = MediaActionModelImpl()
        mediaActionModel.subscribeToActions { assert(false) }
        val obs = mediaActionModel.getMediaActionObservable()
        obs.addListener { _, oldValue, newValue ->
            assert(oldValue == null)
            if (newValue != null) {
                firstSubResultFuture.complete(newValue)
            } else {
                assert(false)
            }
        }
        GlobalScope.launch {
            delay(100)
            mediaActionModel.setSeekAction(50f) {
                assert(false)
            }
        }
        val action = firstSubResultFuture.get()
        assert(action.action == Action.SEEK)
        assert(action.currentTime == 50f)
    }
}