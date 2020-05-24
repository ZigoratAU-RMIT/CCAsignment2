
File='/output.csv'
a<-read.csv(File)

# #--------- First plot on state map
# install.packages('oz')
# library("oz")
# 
# oz(states = TRUE, coast = TRUE, xlim = NULL,
#    ylim = NULL, add = FALSE, ar = 1, eps = 0.25,
#    sections = NULL, visible = NULL)
# points(a$lon, a$lat, col = "red", cex = .2)

#------------ plot second map
install.packages('ggmap')
installed.packages('ggplot2')
library(ggmap)


# Newyour Map limits

b<-a[,3:4];
n1<-which(a[,1]==-73.95690754)
b1<-a[n1,3:4];

n2<-which(a[,1]==-74.00926104)
b2<-a[n2,3:4];

n3<-which(a[,1]==-73.99346227)
b3<-a[n3,3:4];

n4<-which(a[,1]==-73.83285959)
b4<-a[n4,3:4];

n5<-which(a[,1]==-73.98910809)
b5<-a[n5,3:4];

n6<-which(a[,1]==-73.97519054)
b6<-a[n6,3:4];

# road_palette <- c("#900C3F", "#581845",'#C70039','#FF5733','#FFC300','#DAF7A6')
map <- get_map(location=c(left =-74.1, bottom = 40.50, right =  -73.6, top = 41.00))
ggmap(map) +
  geom_point(aes(x = a$d_lon[n1], y = a$d_lat[n1]),data =b1, colour = "#900C3F", size = .05)+
  geom_point(aes(x = a$d_lon[n2], y = a$d_lat[n2]),data =b2, colour = "#581845", size = .05)+
  geom_point(aes(x = a$d_lon[n3], y = a$d_lat[n3]),data =b3, colour = "#C70039", size = .05)+
  geom_point(aes(x = a$d_lon[n4], y = a$d_lat[n4]),data =b4, colour = "#FF5733", size = .05)+
  geom_point(aes(x = a$d_lon[n5], y = a$d_lat[n5]),data =b5, colour = "#FFC300", size = .05)+
  geom_point(aes(x = a$d_lon[n6], y = a$d_lat[n6]),data =b6, colour = "#DAF7A6", size = .05)
# +
  # geom_point(aes(col=road_palette))
