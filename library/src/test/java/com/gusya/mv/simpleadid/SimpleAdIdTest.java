package com.gusya.mv.simpleadid;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.RemoteException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.exceptions.ExceptionIncludingMockitoWarnings;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Gusya on 29/04/2017.
 */
@RunWith(RobolectricTestRunner.class)
public class SimpleAdIdTest {

    @Mock
    Context contextMock;
    @Mock
    PackageManager packageManagerMock;

    @Mock
    SimpleAdId.SimpleAdListener listener;
    @Mock
    SimpleAdId.AdIdInfo adIdInfo;

    Handler mHandler;
    SimpleAdId simpleAdId;

    @Before
    public void beforeTest(){
        MockitoAnnotations.initMocks(this);
        mHandler = new Handler();
        simpleAdId = new SimpleAdId(listener, mHandler);
    }

    @After
    public void afterTest(){
        simpleAdId = null;
        mHandler = null;
    }

    @Test
    public void onSuccessShouldBeCalledOnce(){
        simpleAdId.onSuccessOnUI(adIdInfo);

        verify(listener, times(1)).onSuccess(any(SimpleAdId.AdIdInfo.class));
    }

    @Test
    public void onExceptionShouldBeCalledOnce(){
        simpleAdId.onErrorOnUI(new Exception());

        verify(listener, times(1)).onException(any(Exception.class));
    }

    @Test
    public void onExceptionShouldBeCalledWhenNoPlayServicesInstalled() throws PackageManager.NameNotFoundException{
        when(contextMock.getPackageManager()).thenReturn(packageManagerMock);
        when(packageManagerMock.getPackageInfo("com.android.vending", 0))
                .thenThrow(PackageManager.NameNotFoundException.class);
        final ArgumentCaptor<PackageManager.NameNotFoundException> nameNotFoundExceptionArgumentCaptor =
                ArgumentCaptor.forClass(PackageManager.NameNotFoundException.class);

        new SimpleAdId(listener, mHandler).getGoogleAdIdInfo(contextMock);

        verify(listener, times(1)).onException(nameNotFoundExceptionArgumentCaptor.capture());
        assertEquals(PackageManager.NameNotFoundException.class, nameNotFoundExceptionArgumentCaptor.getValue().getClass());
        assertEquals("package 'com.android.vending' not found", nameNotFoundExceptionArgumentCaptor.getValue().getMessage());
    }

    @Test
    @SuppressWarnings("WrongConstant")
    public void onExceptionShouldBeCalledWhenServiceConnectionFails(){
        when(contextMock.getPackageManager()).thenReturn(packageManagerMock);
        when(contextMock.bindService(any(Intent.class), any(ServiceConnection.class), eq(Context.BIND_AUTO_CREATE)))
                .thenReturn(false);
        final ArgumentCaptor<IllegalStateException> illegalStateExceptionArgumentCaptor =
                ArgumentCaptor.forClass(IllegalStateException.class);

        new SimpleAdId(listener, mHandler).getGoogleAdIdInfo(contextMock);

        verify(listener, times(1)).onException(illegalStateExceptionArgumentCaptor.capture());
        assertEquals(IllegalStateException.class, illegalStateExceptionArgumentCaptor.getValue().getClass());
        assertEquals("Bad GMS service connection", illegalStateExceptionArgumentCaptor.getValue().getMessage());
    }

    @Test
    @SuppressWarnings("WrongConstant")
    public void onExceptionShouldBeCalledWhenRemoteServiceThrowsRemoteException(){
        when(contextMock.getPackageManager()).thenReturn(packageManagerMock);
        // can't reach internal service connection object, so simulate it's error in earlier method
        // inside same try-catch block
        when(contextMock.bindService(any(Intent.class), any(ServiceConnection.class), eq(Context.BIND_AUTO_CREATE)))
                .thenThrow(RemoteException.class);
        final ArgumentCaptor<RemoteException> remoteExceptionArgumentCaptor =
                ArgumentCaptor.forClass(RemoteException.class);

        new SimpleAdId(listener, mHandler).getGoogleAdIdInfo(contextMock);

        verify(listener, times(1)).onException(remoteExceptionArgumentCaptor.capture());
        assertEquals(RemoteException.class, remoteExceptionArgumentCaptor.getValue().getClass());
    }

    @Test
    @SuppressWarnings("WrongConstant")
    public void onExceptionShouldBeCalledWhenRemoteServiceThrowsIllegalStateException(){
        when(contextMock.getPackageManager()).thenReturn(packageManagerMock);
        // can't reach internal service connection object, so simulate it's error in earlier method
        // inside same try-catch block
        when(contextMock.bindService(any(Intent.class), any(ServiceConnection.class), eq(Context.BIND_AUTO_CREATE)))
                .thenThrow(IllegalStateException.class);
        final ArgumentCaptor<IllegalStateException> illegalStateExceptionArgumentCaptor =
                ArgumentCaptor.forClass(IllegalStateException.class);

        new SimpleAdId(listener, mHandler).getGoogleAdIdInfo(contextMock);

        verify(listener, times(1)).onException(illegalStateExceptionArgumentCaptor.capture());
        assertEquals(IllegalStateException.class, illegalStateExceptionArgumentCaptor.getValue().getClass());
    }
}