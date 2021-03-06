# Simple Ad Id
A convinient way to retrieve 'Advertising ID' and 'Is Tracking Enabled' flag without any Play Services dependencies in Android applications.

## Install

Gradle:
```
compile 'com.gusya.mv:simple-adid:1.0.0'
```

Code:
Copy Java classes from library module into your project or build this project as jar\aar file.

## Usage

### Basic

Simply invoke static `SimpleAdId.getAdInfo(Context, SimpleAdListener)` while supplying application context and a listener object.
Be aware that listener's methods will be invoked on UI thread.

```java
SimpleAdId.getAdInfo(getApplicationContext(), new SimpleAdId.SimpleAdListener() {

            @Override
            public void onSuccess(SimpleAdId.AdIdInfo info) {
		String adId = info.getAdId();
		boolean adTrackingEnabled = info.isAdTrackingEnabled();	
            }

            @Override
            public void onException(Exception exception) {
		Log.e("SimpleAdId", exception.getMessage());
		exception.printStackTrace();	
            }
        })
```

### Advanced

Simply invoke static `SimpleAdId.getAdInfo(Context, boolean, SimpleAdListener)` to use predefined thread to invoke listener's methods:
* boolean = true for UI thread
* boolean = false for current thread 

Simply invoke static `SimpleAdId.getAdInfo(Context, IExecutor, SimpleAdListener)` to use your own IExecutor implementation.


# License
```
MIT License

Copyright (c) 2017 Gusya

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
