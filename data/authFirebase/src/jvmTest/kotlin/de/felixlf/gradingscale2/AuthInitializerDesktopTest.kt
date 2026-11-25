package de.felixlf.gradingscale2

import android.app.Application
import com.google.firebase.FirebaseApp
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthInitializerDesktopTest {

    private val testOptions = FirebaseOptions(
        projectId = "test-project",
        applicationId = "1:1234567890:android:abcdef",
        apiKey = "test-api-key",
    )

    @BeforeTest
    fun setUp() {
        FirebaseApp.clearInstancesForTest()
    }

    @AfterTest
    fun tearDown() {
        FirebaseApp.clearInstancesForTest()
    }

    @Test
    fun initializeDesktopFirebaseIsIdempotentSequentiallyAndConcurrently() {
        val threadCount = 8
        val executor = Executors.newFixedThreadPool(threadCount)
        val startLatch = CountDownLatch(1)
        val doneLatch = CountDownLatch(threadCount)
        val errors = mutableListOf<Throwable>()

        try {
            repeat(threadCount) {
                executor.submit {
                    try {
                        startLatch.await()
                        initializeDesktopFirebase(testOptions)
                    } catch (t: Throwable) {
                        synchronized(errors) { errors += t }
                    } finally {
                        doneLatch.countDown()
                    }
                }
            }
            startLatch.countDown()
            assertTrue(doneLatch.await(10, TimeUnit.SECONDS))
        } finally {
            executor.shutdownNow()
        }

        assertTrue(errors.isEmpty(), "Unexpected concurrent init errors: $errors")
        initializeDesktopFirebase(testOptions)
        assertEquals(1, Firebase.apps(Application()).size)
    }

    @Test
    fun invokeIsIdempotentAndDoesNotPrintTokensToStdout() {
        if (BuildResources.FIREBASE_APP_ID.isBlank()) return

        val originalOut = System.out
        val capturedOut = ByteArrayOutputStream()
        try {
            System.setOut(PrintStream(capturedOut))
            val initializer = AuthInitializerImpl()
            initializer.invoke()
            initializer.invoke()
        } finally {
            System.setOut(originalOut)
        }

        val stdoutText = capturedOut.toString()
        assertEquals(1, Firebase.apps(Application()).size)
        assertFalse(stdoutText.contains("Token ="), "Unexpected token output on stdout: $stdoutText")
        assertFalse(stdoutText.contains("tok:"), "Unexpected tok output on stdout: $stdoutText")
    }

    @Test
    fun inMemoryFirebasePlatformHandlesConcurrentStoreRetrieveAndClearSafely() {
        val platform = InMemoryFirebasePlatform()
        val threadCount = 8
        val iterationsPerThread = 200
        val executor = Executors.newFixedThreadPool(threadCount)
        val startLatch = CountDownLatch(1)
        val doneLatch = CountDownLatch(threadCount)
        val errors = mutableListOf<Throwable>()

        try {
            repeat(threadCount) { threadIndex ->
                executor.submit {
                    try {
                        startLatch.await()
                        repeat(iterationsPerThread) { iter ->
                            val key = "key-$threadIndex-$iter"
                            platform.store(key, "val-$iter")
                            assertEquals("val-$iter", platform.retrieve(key))
                            if (iter % 2 == 0) {
                                platform.clear(key)
                                assertNull(platform.retrieve(key))
                            }
                        }
                    } catch (t: Throwable) {
                        synchronized(errors) { errors += t }
                    } finally {
                        doneLatch.countDown()
                    }
                }
            }
            startLatch.countDown()
            assertTrue(doneLatch.await(10, TimeUnit.SECONDS))
        } finally {
            executor.shutdownNow()
        }

        assertTrue(errors.isEmpty(), "Unexpected concurrent map errors: $errors")
        for (t in 0 until threadCount) {
            for (i in 1 until iterationsPerThread step 2) {
                assertEquals("val-$i", platform.retrieve("key-$t-$i"))
            }
        }
    }
}
