# Etapa 1: Compilación
FROM node:22-alpine AS build
WORKDIR /app

# Copiar archivos de dependencias
COPY package*.json ./

# Instalar dependencias limpias
RUN npm ci

# Copiar el resto del código del proyecto
COPY . .

# Compilar la aplicación para producción
RUN npm run build -- --configuration=production

# Etapa 2: Servidor de producción
FROM nginx:alpine

# Copiar los archivos compilados desde la etapa build al directorio de Nginx
# Nota: En Angular moderno, la salida va a dist/client-front/browser
COPY --from=build /app/dist/client-front/browser /usr/share/nginx/html

# Exponer el puerto por defecto de Nginx
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
