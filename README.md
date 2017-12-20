## Ibis - SVG icons for Swing applications

### Overview

The goal of this project is to enable usage of vector-based icons in Java applications. There are two ways to use Ibis APIs for working with vector icons. In both cases the original format is SVG, and in both cases you need the latest stable version of [Apache Batik](https://xmlgraphics.apache.org/batik/) and its dependencies.

### Offline transcoding of SVG content

This is the recommended way of using Ibis. SVG has a wide feature surface which, depending on the complexity of your SVG sources, can lead to significant initial parsing and rendering time of the icon content.

Ibis transcoder allows you to convert an SVG source into a corresponding Java class that contains a sequence of matching Java2D canvas draw calls to render the original content. Ibis ships with three built-in template files that create slightly different wrappers around the Java2D draw calls:

* `SvgTranscoderTemplatePlain.templ` - creates a class with a static method to paint the icon content onto the passed `Graphics2D` object. The class also has methods to query the bounding box of the original SVG content.
* `SvgTranscoderTemplateResizable.templ` - creates a class that implements [Flamingo](https://github.com/kirill-grouchnikov/flamingo)'s `ResizableIcon` interface. An instance of the generated class can be passed to any relevant Flamingo API, including the matching command button and ribbon calls. At runtime, Flamingo will automatically reconfigure the content based on the new display size of the icon. One of the use cases is ribbon resize that may involve a number of command buttons going between smaller and larger icon sizes. It is recommended to use the generated static `of(int, int)` method to obtain an instance of a generated class for the specific initial size.
* `SvgTranscoderTemplateSubstance.templ` - creates a class that implements [Substance](https://github.com/kirill-grouchnikov/flamingo) interfaces for resizable icons that are automatically scaled on high-DPI screens. It is recommended to use the generated static `of(int, int)` and `uiResourceOf` methods to obtain an instance of a generated class for the specific initial size. While not strictly required, such instances can be used directly in custom Substance icon packs configured with the `SubstanceCortex.GlobalScope.setIconPack` API.

The `org.pushingpixels.ibis.SvgBatchConverter` class is the entry point into the offline batch converter pipeline. It has the following parameters:

* [Mandatory] The location of the folder that contains source SVG images
* [Mandatory] The package name for the transcoded classes
* [Mandatory] The path of the template file
* [Optional] The prefix for the class names of the transcoded classes

Assuming your `IBIS_VERSION` and your `FLAMINGO_VERSION` variables point to the latest version of the matching libraries, here is how you would run the converter pipeline for a single SVG source folder:

`java -cp ./flamingo/drop/$FLAMINGO_VERSION/flamingo-$FLAMINGO_VERSION.jar:./ibis/lib/*:./ibis/drop/$IBIS_VERSION/ibis-$IBIS_VERSION.jar org.pushingpixels.ibis.SvgBatchConverter ./flamingo-demo/src/org/pushingpixels/demo/flamingo/svg/tango/transcoded org.pushingpixels.demo.flamingo.svg.tango.transcoded /org/pushingpixels/ibis/SvgTranscoderTemplateResizable.templ`

The first part is enumerating all the jar files required for the converter - Flamingo, Ibis and Batik. The location of these jar files will probably be different on your system.

The second part passes the mandatory parameters:

* `./flamingo-demo/src/org/pushingpixels/demo/flamingo/svg/tango/transcoded` as the location of the source SVG images - and the output transcoded classes
* `org.pushingpixels.demo.flamingo.svg.tango.transcoded` as the package name for the transcoded classes
* `/org/pushingpixels/ibis/SvgTranscoderTemplateResizable.templ` as the path of the template file

### Dynamic display of SVG content at runtime

The second option to display content of SVG icons is to use the `SvgBatikResizableIcon` class. It provides a number of static methods to load the SVG content from an `InputStream` or a `URL`. The source can be either uncompressed or compressed (use `getSvgIcon` or `getSvgzIcon` APIs respectively).

If you choose this route, keep the following in mind:

* You will need to bring in Ibis, Flamingo, Batik and all of Batik's dependencies into your classpath.
* SVG has a wide feature surface which, depending on the complexity of your SVG sources, can lead to significant initial parsing and rendering time of the icon content. In the meantime, you are not showing any graphical content in that specific Swing view.

Note that while `SvgBatikResizableIcon`'s primary intent is to be used with Flamingo components that expect to be populated with resizable icon content, since that class implements the core Swing `Icon` interface, you can use the instance returned by one of the class's static methods anywhere you use icons today.

If your app needs to track the loading of the SVG content, you can use the following APIs on the `SvgBatikResizableIcon` class:

* `isLoading()` API to query whether the content loading has been completed
* `addAsynchronousLoadListener(AsynchronousLoadListener)` to be notified on content load completion
