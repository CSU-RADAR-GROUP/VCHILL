JC = javac -Xlint:all,-serial -g -source 1.5 -target 1.5 -extdirs lib
JD = javadoc -source 1.5 -extdirs lib \
	-d javadoc -windowtitle VCHILL -private -use -version -author\
	-tag pre:X:"Precondition:" \
	-tag post:X:"Postcondition:" \
	-tag created:X:"Created:"
BASEDIR = edu/colostate/vchill
PACKAGE = edu.colostate.vchill

jar:	mainjar ascopejar bookmarkjar cachejar chilljar colorjar connectionjar datajar filejar guijar mapjar netcdfjar numdumpjar plotjar radarjar socketjar proxyjar

all:    jar doc

.java.class:
	$(JC) $<

clean:
	-rm -f *.jar
	-rm -f resources/*.jar
	-rm -f $(BASEDIR)/ascope/*.class
	-rm -f $(BASEDIR)/bookmark/*.class
	-rm -f $(BASEDIR)/cache/*.class
	-rm -f $(BASEDIR)/chill/*.class
	-rm -f $(BASEDIR)/color/*.class
	-rm -f $(BASEDIR)/connection/*.class
	-rm -f $(BASEDIR)/data/*.class
	-rm -f $(BASEDIR)/file/*.class
	-rm -f $(BASEDIR)/gui/*.class
	-rm -f $(BASEDIR)/map/*.class
	-rm -f $(BASEDIR)/netcdf/*.class
	-rm -f $(BASEDIR)/numdump/*.class
	-rm -f $(BASEDIR)/plot/*.class
	-rm -f $(BASEDIR)/proxy/*.class
	-rm -f $(BASEDIR)/radar/*.class
	-rm -f $(BASEDIR)/socket/*.class
	-rm -f $(BASEDIR)/*.class

mainclass:	$(BASEDIR)/*.java
	$(JC) $(BASEDIR)/*.java

ascopeclass:	$(BASEDIR)/ascope/*.java
	$(JC) $(BASEDIR)/ascope/*.java

bookmarkclass:	$(BASEDIR)/bookmark/*.java
	$(JC) $(BASEDIR)/bookmark/*.java

cacheclass:	$(BASEDIR)/cache/*.java
	$(JC) $(BASEDIR)/cache/*.java

chillclass:	$(BASEDIR)/chill/*.java
	$(JC) $(BASEDIR)/chill/*.java

colorclass:	$(BASEDIR)/color/*.java
	$(JC) $(BASEDIR)/color/*.java

connectionclass:	$(BASEDIR)/connection/*.java
	$(JC) $(BASEDIR)/connection/*.java

dataclass:	$(BASEDIR)/data/*.java
	$(JC) $(BASEDIR)/data/*.java

fileclass:	$(BASEDIR)/file/*.java
	$(JC) $(BASEDIR)/file/*.java

guiclass:	$(BASEDIR)/gui/*.java
	$(JC) $(BASEDIR)/gui/*.java

mapclass:	$(BASEDIR)/map/*.java
	$(JC) $(BASEDIR)/map/*.java

netcdfclass:	$(BASEDIR)/netcdf/*.java
	$(JC) $(BASEDIR)/netcdf/*.java

numdumpclass:	$(BASEDIR)/numdump/*.java
	$(JC) $(BASEDIR)/numdump/*.java

plotclass:	$(BASEDIR)/plot/*.java
	$(JC) $(BASEDIR)/plot/*.java

proxyclass:	$(BASEDIR)/proxy/*.java
	$(JC) $(BASEDIR)/proxy/*.java

radarclass:	$(BASEDIR)/radar/*.java
	$(JC) $(BASEDIR)/radar/*.java

socketclass:	 $(BASEDIR)/socket/*.java
	$(JC) $(BASEDIR)/socket/*.java

mainjar:	mainclass resources
	jar cvfm vchill.jar vchill.mf $(BASEDIR)/*.class resources/*.jar resources/icons resources/help

ascopejar:	ascopeclass
	jar cvf ascope.jar $(BASEDIR)/ascope/*.class

bookmarkjar:	bookmarkclass
	jar cvf bookmark.jar $(BASEDIR)/bookmark/*.class

cachejar:	cacheclass
	jar cvf cache.jar $(BASEDIR)/cache/*.class

chilljar:	chillclass
	jar cvf chill.jar $(BASEDIR)/chill/*.class

colorjar:	colorclass
	jar cvfm color.jar color.mf $(BASEDIR)/color/*.class

connectionjar:	connectionclass
	jar cvf connection.jar $(BASEDIR)/connection/*.class

datajar:	dataclass
	jar cvf data.jar $(BASEDIR)/data/*.class

filejar:	fileclass
	jar cvfm file.jar alterer.mf $(BASEDIR)/file/*.class

guijar:	guiclass
	jar cvf gui.jar $(BASEDIR)/gui/*.class

mapjar:	mapclass
	jar cvf map.jar $(BASEDIR)/map/*.class

netcdfjar:	netcdfclass
	jar cvfm netcdf.jar netcdf.mf $(BASEDIR)/netcdf/*.class

numdumpjar:	numdumpclass
	jar cvf numdump.jar $(BASEDIR)/numdump/*.class

plotjar:	plotclass
	jar cvf plot.jar $(BASEDIR)/plot/*.class

proxyjar:	proxyclass
	jar cvfm proxy.jar proxy.mf $(BASEDIR)/proxy/*.class

radarjar:	radarclass
	jar cvf radar.jar $(BASEDIR)/radar/*.class

socketjar:	socketclass
	jar cvf socket.jar $(BASEDIR)/socket/*.class

resources:	resources/colors.jar resources/maps.jar

resources/maps.jar:
	cd resources/maps; jar cvf ../maps.jar *.map; cd ../..

resources/colors.jar:
	cd resources/colors; jar cvf ../colors.jar *.xml; cd ../..

doc:
	rm -rf javadoc/*; \
	$(JD) \
	$(PACKAGE).ascope \
	$(PACKAGE).bookmark \
	$(PACKAGE).cache \
	$(PACKAGE).chill \
	$(PACKAGE).color \
	$(PACKAGE).connection \
	$(PACKAGE).data \
	$(PACKAGE).file \
	$(PACKAGE).gui \
	$(PACKAGE).map \
	$(PACKAGE).netcdf \
	$(PACKAGE).numdump \
	$(PACKAGE).plot \
	$(PACKAGE).proxy \
	$(PACKAGE).radar \
	$(PACKAGE).socket \
	$(PACKAGE)

install:	jar *.css *.html *.jnlp *.php *.xml
	signjars
	scp -P 20789 -p *.css *.html *.jar *.jnlp *.php *.pdf *.xml lab:www
	scp -P 20789 -p lib/*.jar lab:www/lib
